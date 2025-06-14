# IANA-maintained models

This directory contains model packaging for models listed in the IANA
[YANG Module Names registry](https://www.iana.org/assignments/yang-parameters/yang-parameters.xhtml#yang-parameters-1)
marked as being maintained by IANA. These typically start with `iana-`.

We package exactly one revision of each model. The revisions of models can be bumped across major and minor versions
(e.g between 15.0.2 and 15.1.0), but not across minor versions (e.g. between 15.0.2 and 15.0.3).

YANG files included here should always come from the above IANA registry.

Model currently packaged are:
* [iana-crypt-hash.yang](iana-crypt-hash)
* [iana-hardware.yang]( iana-hardware)
* [iana-if-type.yang](iana-if-type)
* [iana-routing-types.yang](iana-routing-types)
* [iana-ssh-encryption-algs.yang](iana-ssh-encryption-algs)
* [iana-ssh-key-exchange-algs.yang](iana-ssh-key-exchange-algs)
* [iana-ssh-mac-algs.yang](iana-ssh-mac-algs)
* [iana-ssh-public-key-algs.yang](iana-ssh-public-key-algs)
* [iana-tls-cipher-suite-algs.yang](iana-tls-cipher-suite-algs)
