FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}/:"

SRC_URI += " \
    file://0001-gst-launch-Add-padprobe-tool-to-measure-fps-in-sink-.patch \
    file://0002-baseparse-Revert-fix-upstream-read-caching.patch \
"
