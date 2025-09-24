#!/bin/sh
# -----------------------------------------------------------------------------
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
# -----------------------------------------------------------------------------

# Build parameter:
#   --build   Run 'mvn clean install -Pq' once before migration
if [ $# -gt 1 ]; then
  echo "Usage: $0 [--build]" >&2
  exit 2
fi
DO_BUILD=0
if [ "${1:-}" = "--build" ]; then
  DO_BUILD=1
elif [ -n "${1:-}" ]; then
  echo "Unknown option: $1" >&2
  echo "Usage: $0 [--build]" >&2
  exit 2
fi

# 1. Build once before migration (only if --build was passed)
if [ "$DO_BUILD" -eq 1 ]; then
  ( cd .. && mvn clean install -Pq )
  echo "Maven build: mvn clean install -Pq"
else
  echo "Skipping build (pass --build to enable)"
fi

# 2. Delete legacy src/main/feature/feature.xml if present
echo "List of removed files:"
find . -type f -path '*/src/main/feature/feature.xml' -print -exec rm -f {} +

# 3. Move generated feature descriptors -> template.xml
echo "Moving generated feature.xml files to src/main/template.xml:"
find . -type f -path '*/target/feature/feature.xml' | while IFS= read -r file;
do
  # file = <module>/target/feature/feature.xml
  mod_dir=$(dirname "$(dirname "$(dirname "$file")")")  # -> <module>
  dest="$mod_dir/src/main/feature/template.xml"
  mkdir -p "$(dirname "$dest")"
  cp -f "$file" "$dest"
  echo "Copied: $file -> $dest"

  # 4. Normalize XML prolog and insert copyright header
  XML_DECL='<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
  XML_HDR='<!--
   * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
   *
   * This program and the accompanying materials are made available under the
   * terms of the Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
-->'

  # Remove any XML declarations anywhere
  sed -E '/^[[:space:]]*<\?xml/d' "$dest" > "$dest.noxml"

  # Remove an existing EPL header block if present (from <!-- to --> containing the EPL marker)
  awk '
    BEGIN { in_hdr=0 }
    /<!--/ {
      if ($0 ~ /Eclipse Public License v1\.0/) { in_hdr=1 }
    }
    {
      if (in_hdr) {
        if ($0 ~ /-->/) { in_hdr=0 }
        next
      }
      print
    }
  ' "$dest.noxml" > "$dest.clean"

  # Trim leading blank lines
  sed -E '1{/^[[:space:]]*$/d;}' "$dest.clean" > "$dest.body"

  # Rebuild with a XML declaration + header at the top
  { printf '%s\n' "$XML_DECL"; printf '%s\n' "$XML_HDR"; cat "$dest.body"; } > "$dest.tmp" && mv "$dest.tmp" "$dest"
  rm -f "$dest.noxml" "$dest.clean" "$dest.body"

  # 5 Apply mustaches inside template.xml

    # 5.1 Replace versions in mvn coordinates with {{versionAsInProject}}
    #     mvn:group/artifact/<version>[/...]  -> mvn:group/artifact/{{versionAsInProject}}[/...]
    # shellcheck disable=SC2016
    sed -E 's#(mvn:[^/:]+/[^/]+/)[^/<"]+#\1{{versionAsInProject}}#g' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"

    # 5.2 On <configfile> lines, use ${project.version} instead
    sed -E '/<configfile/ s#(mvn:[^/:]+/[^/]+/)[^/<"]+#\1${project.version}#g' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"

    # 5.3 For dependency features (lines without name):
    # - range versions -> {{semVerRange}}
    # - exact versions -> {{versionAsInProject}}
    awk '{
      line = $0
      if (line ~ /<feature/ && line !~ /name=/ && line ~ /version="[^"]+"/) {
        if (line ~ /version="(\(|\[)[^"]*,[^"]*(\)|\])"/) {
          # Range like [14,15) or (12,15]
          sub(/version="[^"]+"/, "version=\"{{semVerRange}}\"", line)
        } else {
          # Exact version like 15.0.1.SNAPSHOT
          sub(/version="[^"]+"/, "version=\"{{versionAsInProject}}\"", line)
        }
        print line
      } else {
        print
      }
    }' "$dest" > "$dest.tmp" && mv "$dest.tmp" "$dest"

    # 5.4 Strip prerequisite/dependency flags from dependency <feature> (no name=)
      awk '{
        line = $0
        if (line ~ /<feature/ && line !~ /name=/) {
          gsub(/[[:space:]]+(prerequisite|dependency)="[^"]*"/, "", line)
          print line
        } else {
          print
        }
      }' "$dest" > "$dest.tmp" && mv "$dest.tmp" "$dest"

    # 5.5 Clean top-level <feature name="...">: remove description= and version= attributes
      # (do NOT touch dependency <feature> tags without name=)
      sed -E '/<feature[^>]*name=/{ s/[[:space:]]+(description|version)="[^"]*"//g }' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"

    # 5.6 Remove any <details>…</details> lines
      sed -E '/<details>.*<\/details>/d' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"
done

# 6. Update parent in pom.xml for each feature module
#    - Change <artifactId>feature-parent</artifactId> -> template-feature-parent
#    - Change <relativePath>../feature-parent[...]</relativePath> -> ../parent[...]
echo "Updating parent section in pom.xml for feature modules:"
find . -name pom.xml -not -path '*/feature-parent/*' -not -path '*/parent/*' | while IFS= read -r pom;
do
  sed -E '/<parent>/,/<\/parent>/{
    s#(<artifactId>)[[:space:]]*feature-parent[[:space:]]*(</artifactId>)#\1template-feature-parent\2#g
    s#(<relativePath>[^<]*)\.\./feature-parent#\1../parent#g
  }' "$pom" > "$pom.sed" && mv "$pom.sed" "$pom"
  echo "  Updated: $pom"
done

# 5.6 Normalize top-level <details>: add an empty <description/> if no exists
echo "Ensuring each feature POM (excluding root) has an <description/> ..."
find . -mindepth 2 -name pom.xml \
  -not -path '*/feature-parent/*' -not -path '*/parent/*' | while IFS= read -r pom
do
  # Skip if a top-level <description> already exists
  if grep -Eq '^[[:space:]]*<description(/>|>.*</description>)[[:space:]]*$' "$pom"; then
    echo "  OK (has description): $pom"
    continue
  fi

  awk '
    {
      n++; lines[n]=$0
    }
    END {
      has_desc=0; in_parent=0; name_idx=0; parent_end_idx=0
      # 1) scan for existing description, name (outside parent), and </parent>
      for (i=1; i<=n; i++) {
        line = lines[i]
        if (line ~ /^[[:space:]]*<description(\/>|>.*<\/description>)[[:space:]]*$/) has_desc=1
        if (line ~ /<parent>/) in_parent=1
        if (!in_parent && name_idx==0 && line ~ /^[[:space:]]*<name>.*<\/name>[[:space:]]*$/) name_idx=i
        if (line ~ /<\/parent>/) { parent_end_idx=i; in_parent=0 }
      }
      if (has_desc) {
        for (i=1; i<=n; i++) print lines[i]
        exit
      }
      # 2) choose insertion point: after <name> if present, else after </parent>
      ins_idx = (name_idx > 0 ? name_idx : parent_end_idx)
      indent=""
      if (ins_idx > 0 && match(lines[ins_idx], /^([[:space:]]*)</, m)) indent=m[1]

      for (i=1; i<=n; i++) {
        print lines[i]
        if (i == ins_idx) {
          print indent "<description/>"
        }
      }
    }
  ' "$pom" > "$pom.sed" && mv "$pom.sed" "$pom"

  echo "  Added <description/> after <name> (or </parent> fallback): $pom"
done
