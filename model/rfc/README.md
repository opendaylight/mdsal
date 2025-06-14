# IETF-maintained models

This directory contains model packaging for models listed in the IANA
[YANG Module Names registry](https://www.iana.org/assignments/yang-parameters/yang-parameters.xhtml#yang-parameters-1)
marked as *NOT* being maintained by IANA. These typically start with `ietf-`.

Each model is packaged in a separate artifact, indicating its lifecycle:
* artifacts starting with `rfc` package the models published by that RFC
* a single YANG module *MAY* be packaged in multiple artifacts for compatibility, but generally we strive for not
  having multiple revisions

New artifacts may be introduced in minor versions (e.g. between 15.0.2 and 15.0.3), but can only be removed, or changed
incompatibly in a major version (e.g between 15.x.y and 16.0.0).

YANG files included here should always come from the above IANA registry.
