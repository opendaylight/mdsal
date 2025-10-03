#!/bin/sh

# If run from repo root (has ./features), search there; otherwise assume we're inside ./features.
if [ -d "./features" ]; then
  BUILD_DIR="."
  SEARCH_DIR="./features"
else
  BUILD_DIR=".."
  SEARCH_DIR="."
fi

# 1. Build first
#( cd "$BUILD_DIR" && mvn clean install -Pq )
echo "Maven build: mvn clean install -Pq"

# 2. Delete legacy src/main/feature/feature.xml if present
echo "List of removed files:"
find "$SEARCH_DIR" -type f -path '*/src/main/feature/feature.xml' -print -exec rm -f {} +

# 3. Move generated feature descriptors -> template.xml
echo "Moving generated feature.xml files to src/main/template.xml:"
find "$SEARCH_DIR" -type f -path '*/target/feature/feature.xml' | while IFS= read -r file
do
  # file = <module>/target/feature/feature.xml
  mod_dir=$(dirname "$(dirname "$(dirname "$file")")")  # -> <module>
  dest="$mod_dir/src/main/feature/template.xml"
  mkdir -p "$(dirname "$dest")"
  cp -f "$file" "$dest"
  echo "Copied: $file -> $dest"

  # 4 Apply mustaches inside template.xml

    # 4.1 Replace versions in mvn coordinates with {{versionAsInProject}}
    #     mvn:group/artifact/<version>[/...]  -> mvn:group/artifact/{{versionAsInProject}}[/...]
    # shellcheck disable=SC2016
    sed -E 's#(mvn:[^/:]+/[^/]+/)[^/<"]+#\1{{versionAsInProject}}#g' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"

    # 4.2 For dependency features (lines without name):
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

    # 4.3 Clean top-level <feature name="...">: remove description= and version= attributes
      # (do NOT touch dependency <feature> tags without name=)
      sed -E '/<feature[^>]*name=/{ s/[[:space:]]+(description|version)="[^"]*"//g }' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"

    # 4.4 Remove any <details>…</details> lines
      sed -E '/<details>.*<\/details>/d' "$dest" > "$dest.sed" && mv "$dest.sed" "$dest"
done

# 5. Update parent in pom.xml for each feature module
#    - artifactId: feature-parent -> template-feature-parent
#    - relativePath: ../feature-parent[/...] -> ../parent[/...]
echo "Updating parent section in pom.xml for feature modules:"
find "$SEARCH_DIR" -name pom.xml \
  -not -path '*/feature-parent/*' -not -path '*/parent/*' | while IFS= read -r pom
do
  # Only modify inside the <parent>...</parent> section
  sed -E '/<parent>/,/<\/parent>/{
    s#(<artifactId>)feature-parent(</artifactId>)#\1template-feature-parent\2#g
    s#(\<relativePath\>[^<]*)\.\./feature-parent#\1../parent#g
  }' "$pom" > "$pom.sed" && mv "$pom.sed" "$pom"
  echo "  Updated: $pom"
done
