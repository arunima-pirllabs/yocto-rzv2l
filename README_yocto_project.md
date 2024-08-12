# YOCTO PROJECT

## GETTING STARTED

Begin by sourcing this script
```bash
source ./oe-init-build-env <builddir>
```

- builder here is the working directory, default `build/`
- changes into that directory
- the directory path is stored in shell varible $BUILDDIR
- to return to this directory later, just `$ cd $BUILDDIR`

*NOTE*: you must source `source ./oe-init-build-env <builddir>` each time you use OpenEmbedded from a new shell.

### Yocto directory layout

```bash
poky/
|-- bitbake/
|-- build/
|   | -- conf/
|   |    |--bblayers.conf
|   |    `--local.conf
|-- documentation/
|-- meta/
|-- meta-poky/
|-- meta-yocto-bsk/
|-- oe-init-build-env/
"
```

### Setting the build config
- Build config files are in `$BUILDDIR/conf/`
- The initial set is:
  - `bblayers.conf`: list of directories containing layers
  - `local.conf`: local config

### Config, layer and recipie
```
                                                DISTRO
                                                ▲
                                                │
                    ┌──────────────────────────┐│
│---build/conf------│ bblayers.conf local.conf ┼┤
│                   └──────────────────────────┘│
│                                               │
│                                               ▼
│                   ┌──────────────┐            MACHINE
│  xxxxxxx xxxxx    │              │
│--xmeta-------x----│glibc_2.22.bb ┼───►RECIPES
│  x           x    │              │
│  x           x    └──────────────┘
│  x           x
│  x           x    ┌───────────┐
│--xmeta-pocky-x----│tiny-int.bb│
   xxxxxxxxxxxxx    └───────────┘
         │
         └────► LAYERS
```

- Distro: how i want to put my system together
- Machine: the board i want to build for
- Images: the selection of packages i want

### Distro

- In `conf/local.conf`, selected by DISTRO
- Example
  - `DISTRO ?= "poky"
- This selects the Poky distro, defined in meta-poky/conf/distro/poky.conf

### Machine

- In `conf/local.conf`, MACHINE selects target machine
- Example:
  - `MACHINE := "beaglebone-yocto"`
- Each machine has a corresponding configuration file in `<layer>/conf/machine/<machine>.conf`
- For Beaglebone black it is: `meta-yocto-bsp/conf/machine/beaglebone-yocto.conf`

### Bitbake and recipes

- OpenEmbedded uses BitBake to build the target
- Bitbake reads recipes to create a dependency tree
- Then executes all recipes required to build the final target
- to being with we will build an image recipe

### Standard image recipes

- core-image-minimal: small console based image, useful for tests and as the basis for custom images
- core-image-base: a console only image that fully supports the target device
- core-full-cmdline: a console only image with fully-features Linux system functionality installed
- core-image-x11: small X11 server based graphical system, including xterminal
- core-image-sato: full graphical system based on Sato (a mobile GUI built on X11 and GNOME)

### Building an image

- to build image, simply run BitBake and the image name
  - genearally, to build any recipe, give it as a parameter to BitBake
- `bitbake core-image-minimal` (example if we want to use core-image-minimal)

**NOTE**: this will take a lot of time.

### Build artifacts

```bash
|-- cache/
|-- conf/
|-- downloads/
|-- sstate-cache/
|-- tmp/
    |-- deploy/
    |   |-- images/
    |   |   `-- qemuarm
    |   |-- licenses/
    |   `-- rpm/
    `-- work/
```

```bash
|-- cache/        (Locally cached state)
|-- conf/
|-- downloads/    (Things that are downloaded for settig up the env, *Souce code and other upstream tarballs*)
|-- sstate-cache/ (Shared state cache, you can make this an NFS export and share amongst a group of developers)
|-- tmp/          (IMP: The build artifacts)
    |-- deploy/   (Code to be deployed to the target)
    |   |-- images/
    |   |   `-- qemuarm (This contains the compiled image that we can use)
    |   |-- licenses/
    |   `-- rpm/
    |-- buildstats/(usefull information about CPU usage, time taken)
    `-- work/      (The staging (build) area for packages, *work is not needed after a build: can be safely deleted*)
```

### Image formats

- The ultimate output of the build as a set of image files that can be programmed into flash memory of the target device
- Yocto can generate different formats:
  - tar file: extract into formatted partition
  - partition image (ext4): raw copy to disk or MTD partition
  - disk image (wic): raw copy to disk

#### Settings image file

- list of formats you want in the machine conf file
- `IMAGE_FSTYPES = "ext3 tar.bz2"`

**NOTE**: this part is usually not required to be edited

### Shared state cache

- Binary build artifacts are put into the shared state chache
  - speeds up subsequent build
  - can be shared with other developers
- When running BitBake, you will notice
  - Building from shared state cache:
    - `NOTE: Executing SetScene Tasks`
  - Building from source
    - `NOTE: Executing RunQueue Tasks`
- Pruning the sstate `sstate-cache-management.sh --cache-dir=state-cache -d`

### Build directory paths

- Location of build directories is set by these variables (which you can change in local.conf)
- The default settings are relative to BitBake variable `$TOPDIR` which is identical to shell variable `$BUILDDIR`

| *Variable*  | *Default*                 | *Purpose*       |
| SSTATE_DIR  | "${TOPDIR}/sstate-chache" | Shared state    |
| TMPDIR      | "${TOPDIR}/tmp"           | Build artifacts |
| DL_DIR      | "${TOPDIR}/downloads"     | Downloads       |

## Layers

- The layers actively used in a project are listed in conf/bblayers.conf

initially bblayers.conf looks like this:

```conf
POCKY_BBLAYERS_CONF_VERSION = "2"

BBPATH = "${TOPDIR}"
BBFILES ?= ""

BBLAYERS ?= "\
    /home/chris/pocky/meta \
    /home/chris/pocky/meta-pocky \
    /home/chris/pocky/meta-yocto-bsp \
"
```

There are 3 types of layers:

- *BSP*: defines a machine and related board specific packages (Board support package)
  - contains `conf/machine/[MACHINE].conf`
- *Distribution*: defines a distro such as Poky or Angstrom
- *Software*: everything else
  - contains neither `conf/machine[MACHINE].conf` not `conf/distro/[DISTRO].conf`
  - libraries eg: `qt5`
  - languages eg: `Java`
  - tools, eg: virtualisation or selinux

Have a look at open-embedded layer index website for information on the layers: `https://layers.openembedded.org/layerindex/branch/master/layers/`

*NOTE*: for software that i can require i can get the entire layer instead of a single recipe.

**NOTE**: add the layer from github to the meta-openembeded dir

To add layers to the project when it is running simply do:
`bitbake-layers add-layer <name of layer>`

To check if a recipie is avaialable:
`bitbake-layers show-recipes -f <nano of recipe eg: nano>`

after doing this we need to go ahead and add:

```bash
IMAGE_INSTALL:append = "nano"
```

this will install the recipe nano from the layers.

then after that we need to call `bitbake core-image-base` (base core image).

### Recipes

- Contains instructions on how to fetch, configure, compile and install a software component
- The body contains BitBake metadata (assignment of variables, mostly); the tasks are written in shell script or Python
- Recipe files have suffix `.bb`
- May be extended with append recipies with `.bbappend` suffix

**EXAMPLE**: simple recipe that builds a "helloworld" program:
`poky/recipes-skeleton/hello-single/hello_1.0.bb`

```bash
DESCRIPTION = "Simple hello world program"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=<random number>"

SRC_URI = "file://helloworld.c"

S = "${WORKDIR}"

do_compile() {
    ${CC} ${LDFLAGS} helloworld.c -o helloworld
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 helloworld ${D}${bindir}
}
```

*Explanation of recipie*:

```bash
DESCRIPTION = "Simple hello world program"
SECTION = "examples"
LICENSE = "MIT" (everything needs a license)
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=<random number>" (need to give checksum of the text)

SRC_URI = "file://helloworld.c"

S = "${WORKDIR}"

# Compile hello world
do_compile() {
    ${CC} ${LDFLAGS} helloworld.c -o helloworld
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 helloworld ${D}${bindir}
}
```

#### Packages and recipes

- The majority of recipes produce packages
- Often one recipe produces several packages, for example libz:

```bash
cd tmp/deploy/rpm/cortexa57
ls libz*
```

#### Adding a package

- image recipes, such as `core-image-minimal` contain a list of packages in `IMAGE_INSTALL`
- you can add extra packages by appending `IMAGE_INSTALL` in `conf/local.conf`
- example: `IMAGE_INSTALL:append = " dropbear lighttpd`
    - Note the leading space before the backage name (dropbear)
    - Another way to achieve the same result for core image recipes is:
      - `CORE_IMAGE_EXTRA_INSTALL += "dropbear lighthttpd`

#### Adding a custom recipe

- Need to createa a layer first, createa a custom layer
  - `bitbake-layers create-layer ../meta-example`
  - Add layer using `bitbake-layers add-layer ../meta-example`

Directory structure of the custom layer

```bash
|-- example
|   `- example_0.1.bb
|-- helloworld
    |-- files
    |   `-helloworld.c
    |-- helloworld_1.0.bb
```

```bash
|-- example
|   `- example_0.1.bb
|-- helloworld (this is the new recipe that was added in the new custom layer)
    |-- files
    |   `-helloworld.c
    |-- helloworld_1.0.bb
```

## BUILDING THE SDK

**NOTE**: not sure as to what this does but it does start the process of downloading some dependencies that are realated
  to QEMU and it is the only reason that I am going to be running this command for the QEMU emulation process

```bitbake <name-of-image> -c populate_sdk```

# QEMU SETUP

## QEMU command to start emulation

```bash
qemu-system-aarch64 -M virt -m 1024 -nographic \
-kernel /path/to/Image-smarc-rzv2l.bin \
-dtb /path/to/r9a07g054l2-smarc.dtb \
-append "root=/dev/vda rw console=ttyAMA0" \
-drive file=/path/to/rootfs.ext4,if=virtio,format=raw
```


```bash
qemu-system-arm \
    -M versatilepb \  # or the appropriate machine type for your BSP
    -m 512 \          # specify the amount of RAM
    -kernel /path/to/Image \  # path to your kernel image
    -dtb /path/to/r9a07g054l2-smarc.dtb \  # path to your DTB file
    -drive file=/path/to/core-image-minimal-smarc-rzv2l-<timestamp>.rootfs.ext4,format=raw,if=virtio \  # path to your root filesystem
    -append "root=/dev/vda rw console=ttyAMA0" \  # kernel command line parameters
    -nographic \      # run in non-graphical mode (optional)
```

```bash
qemu-system-aarch64 \
    -M virt \
    -m 2048 \
    -kernel /home/rahul/workspace/yocto-rzv2l/build/tmp/deploy/images/smarc-rzv2l/Image-smarc-rzv2l.bin \
    -dtb /home/rahul/workspace/yocto-rzv2l/build/tmp/deploy/images/smarc-rzv2l/Image-r9a07g054l2-smarc.dtb \
    -drive file=/home/rahul/workspace/yocto-rzv2l/build/tmp/deploy/images/smarc-rzv2l/core-image-minimal-smarc-rzv2l.ext4,format=raw,if=virtio \
    -append "root=/dev/vda rw console=ttyAMA0" \
    -nographic
```

**These are the following steps that are used for**
sudo cp /home/pirllabs/Renesas/6thEnergy-RZV2L-304/build/tmp/deploy/images/smarc-rzv2l/Image-smarc-rzv2l.bin /media/pirllabs/09F7-5104
sudo cp /home/pirllabs/Renesas/6thEnergy-RZV2L-304/build/tmp/deploy/images/smarc-rzv2l/r9a07g054l2-smarc.dtb /media/pirllabs/09F7-5104
cd /mnt/rootfs
sudo tar jxvf /home/pirllabs/Renesas/6thEnergy-RZV2L-304/build/tmp/deploy/images/smarc-rzv2l/core-image-weston-smarc-rzv2l.tar.bz2
sync

# LINKS AND OTHER USEFULL SOURCES:
- https://github.com/renesas-rz/meta-renesdoes tas (Info on how on dependencies for yocto project on the renesas board)
- https://github.com/renesas-rz/rzv2h_drp-ai_driver (Link to the DRPAI drivers sometime it is required for the video codec processing)
