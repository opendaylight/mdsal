This directory is only for bug reporting purposes.
It should NOT be part of ODL as is,
it should be reworked into a lightweight unit test instead.

The Yang models used are subset of
https://github.com/YangModels/yang
(commit c6b8976bc49522250e5cc121f6a98a23c75ebd98)
which showcase a NullPointerException,
pointing at a Bug, possibly in Yantools.

The models are placed in Mdsal project,
because binding-parent is the simplest way to show the Bug.
ietf-ipv4-unicast-routing.yang is the file triggering the Bug,
other Yang models are there just as dependencies
as ODL may already contain different revisions of them.

Also, the models are all IETF drafts or RFCs.
