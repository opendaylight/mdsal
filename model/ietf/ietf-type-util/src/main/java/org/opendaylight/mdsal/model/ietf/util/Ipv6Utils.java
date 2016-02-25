/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * IPv6 address parsing for ietf-inet-types ipv6-address and ipv6-prefix. This is an internal implementation
 * class, not meant to be exposed in any shape or form to the outside world, as the code relies on the fact that
 * the strings presented to it have been previously validated to conform to the regular expressions defined in
 * the YANG model.
 */
/*
 * v6 routines added by Anton Ivanov on 14.6.2015
 * revised by Robert Varga
 *
 * BIG FAT WARNING!!!
 * Read all of the following before you touch any v6 code or decide to
 * optimize it by invoking a "simple" Guava call
 *
 * Java IPv6 is fundamentally broken and Google libraries do not fix it.
 * 1. Java will allways implicitly rewrite v4 mapped into v6 as a v4 address
 *      and there is absolutely no way to override this behaviour
 * 2. Guava libraries cannot parse non-canonical IPv6. They will throw an
 *      exception. Even if they did, they re-use the same broken java code
 *      underneath.
 *
 * This is why we have to parse v6 by ourselves.
 *
 * The following conversion code is based on inet_cidr_pton_ipv6 in NetBSD
 *
 * The original BSD code is licensed under standard BSD license. While we
 * are not obliged to provide an attribution, credit where credit is due.
 * As far as why it is similar to Sun's sun.net.util please ask Sun why
 * their code has the same variable names, comments and code flow.
 */
final class Ipv6Utils {
    private static final int INADDR4SZ = 4;
    private static final int INADDR6SZ = 16;
    private static final int INT16SZ = 2;

    private Ipv6Utils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert Ipv6Address object to a valid Canonical v6 address in byte format
     *
     * @param ipv6Address - v6 Address object
     *
     * FIXME: rovarga: this looks wrong
     * @return - byte array of size 16. Last byte contains netmask
     */
   public static byte[] bytesForString(final String ipv6Address) {
       /*
        * Do not modify this routine to take direct strings input!!!
        * Key checks have been removed based on the assumption that
        * the input is validated via regexps in Ipv6Prefix()
        */

       String [] address =  (ipv6Address).split("%");

       int colonp;
       char ch;
       boolean saw_xdigit;

       /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

       int val;

       char[] src = address[0].toCharArray();

       byte[] dst = new byte[INADDR6SZ];

       int src_length = src.length;

       colonp = -1;
       int i = 0, j = 0;

       /* Leading :: requires some special handling. */

       /* Isn't it fun - the above comment is again the same in BSD and Sun sources,
        * We will derive our code from BSD. Shakespear always sounds better
        * in original Clingon. So does Dilbert.
        */

       if (src[i] == ':') {
           Preconditions.checkArgument(src[++i] == ':', "Invalid v6 address");
       }

       int curtok = i;
       saw_xdigit = false;


       val = 0;
       while (i < src_length) {
           ch = src[i++];
           int chval = Character.digit(ch, 16);

           /* Business as usual - ipv6 address digit.
            * We can remove all checks from the original BSD code because
            * the regexp has already verified that we are not being fed
            * anything bigger than 0xffff between the separators.
            */

           if (chval != -1) {
               val <<= 4;
               val |= chval;
               saw_xdigit = true;
               continue;
           }

           /* v6 separator */

           if (ch == ':') {
               curtok = i;
               if (!saw_xdigit) {
                   /* no need to check separator position validity - regexp does that */
                   colonp = j;
                   continue;
               }

               /* removed overrun check - the regexp checks for valid data */

               dst[j++] = (byte) ((val >>> 8) & 0xff);
               dst[j++] = (byte) (val & 0xff);
               saw_xdigit = false;
               val = 0;
               continue;
           }

           /* frankenstein - v4 attached to v6, mixed notation */

           if (ch == '.' && ((j + INADDR4SZ) <= INADDR6SZ)) {

               /* this has passed the regexp so it is fairly safe to parse it
                * straight away. As v4 addresses do not suffer from the same
                * defficiencies as the java v6 implementation we can invoke it
                * straight away and be done with it
                */

               Preconditions.checkArgument(j != (INADDR6SZ - INADDR4SZ - 1), "Invalid v4 in v6 mapping");

               InetAddress _inet_form = InetAddresses.forString(address[0].substring(curtok, src_length));

               Preconditions.checkArgument(_inet_form instanceof Inet4Address);
               System.arraycopy(_inet_form.getAddress(), 0, dst, j, INADDR4SZ);
               j += INADDR4SZ;

               saw_xdigit = false;
               break;
           }
           /* removed parser exit on invalid char - no need to do it, regexp checks it */
       }
       if (saw_xdigit) {
           Preconditions.checkArgument(j + INT16SZ <= INADDR6SZ, "Overrun in v6 parsing, should not occur");
           dst[j++] = (byte) ((val >> 8) & 0xff);
           dst[j++] = (byte) (val & 0xff);
       }

       if (colonp != -1) {
           int n = j - colonp;

           Preconditions.checkArgument(j != INADDR6SZ, "Overrun in v6 parsing, should not occur");
           for (i = 1; i <= n; i++) {
               dst[INADDR6SZ - i] = dst[colonp + n - i];
               dst[colonp + n - i] = 0;
           }
           j = INADDR6SZ;
       }

       Preconditions.checkArgument(j == INADDR6SZ, "Overrun in v6 parsing, should not occur");

       return dst;
   }

}
