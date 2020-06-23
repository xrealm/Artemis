# !/usr/bin/env python3
# -*- coding: utf-8 -*-

__author__ = 'xrealm'

import sys

f = sys.stdin
if len(sys.argv) > 1:
    f = open(sys.argv[1])
else:
    exit(0)

lines = f.readlines()
print('"' + lines[0].rstrip() + '\\n\"')
for line in lines[1:]:
    print('+ "' + line.rstrip() + '\\n\"')
