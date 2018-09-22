#!/usr/bin/env bash

// сам пока не пробовал
// источник: https://gist.github.com/calebbrewer/aca424fb14618df8aadd



yum install fontconfig libXrender libXext xorg-x11-fonts-Type1 xorg-x11-fonts-75dpi freetype libpng zlib libjpeg-turbo

wget https://downloads.wkhtmltopdf.org/0.12/0.12.2.1/wkhtmltox-0.12.2.1_linux-centos7-amd64.rpm

rpm -Uvh wkhtmltox-0.12.2.1_linux-centos7-amd64.rpm
