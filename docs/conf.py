#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# SPDX-License-Identifier: EPL-1.0
##############################################################################
# Copyright (c) 2018 The Linux Foundation and others.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
##############################################################################

import xml.etree.ElementTree as ET
import subprocess

from docs_conf.conf import *

def format_version(version):
    fmt = '{tag}.dev{commitcount}+{gitsha}'
    parts = version.split('-')
    assert len(parts) in (3, 4)
    dirty = len(parts) == 4
    tag, count, sha = parts[:3]
    if count == '0' and not dirty:
        return tag
    return fmt.format(tag=tag, commitcount=count, gitsha=sha.lstrip('g'))

extensions.append('sphinx.ext.extlinks')
extensions.append('sphinxcontrib.plantuml')

extlinks = {
    'mdsal-apidoc': ('https://github.com/sphinx-doc/sphinx/issues/%s', 'api '),
}

data = ET.parse('pom.xml')
mdsal_version = data.getroot().find('*//{http://maven.apache.org/POM/4.0.0}version').text
version = mdsal_version
release = mdsal_version
