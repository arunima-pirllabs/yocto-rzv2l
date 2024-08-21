SUMMARY = "BitBake recipe for OS02G10 camera driver"
SECTION = "kernel"
LICENSE = "MIT"

LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=030cb33d2af49ccebca74d0588b84a21 \
"

PR = "p0"
PN = "kernel-module-os02g10"

SRC_URI = " \
    file://recipes-kernel/linux/files/drivers/media/i2c/os02g10.c \
    file://recipes-kernel/linux/files/drivers/media/i2c/os02g10.c/Kconfig \
    file://recipes-kernel/linux/files/drivers/media/i2c/os02g10.c/Makefile \
"

S = "${WORKDIR}"

COMPATIBLE_MACHINE = "(r9a07g044l|r9a07g054l|r9a07g044c)"

EXTRA_OEMAKE = 'KDIR="${STAGING_KERNEL_DIR}" \
                ARCH="${ARCH}" \
                CROSS_COMPILE="${CROSS_COMPILE}" \
                '

module_do_compile() {
    # Build the module using the kernel’s Makefile system
    make -C ${STAGING_KERNEL_DIR} M=${S} modules
}

module_do_install() {
    # Install the module into the appropriate directory
    install -d ${D}/lib/modules/${KERNEL_VERSION}/
    install -m 644 ${S}/os02g10.ko ${D}/lib/modules/${KERNEL_VERSION}/
}

FILES_${PN} = " \
    /lib/modules/${KERNEL_VERSION}/os02g10.ko \
"

PACKAGES = "${PN}"

RPROVIDES_${PN} += "kernel-module-os02g10"

KERNEL_MODULE_AUTOLOAD_append = " os02g10"
