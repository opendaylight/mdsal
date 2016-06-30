This directory is only for bug reporting purposes.
It should NOT be part of ODL as is,
it should be reworked into a lightweight unit test instead.

Leafref points to leaf with type referring to typedef of enum type.
That leads to IllegalArgumentException in provideTypeForLeafref(TypeProviderImpl.java:510)
If the type is inlined (without typedef), it works.
If the typedef is not enum (string ot int64 for example), it works.
