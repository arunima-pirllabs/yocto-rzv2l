# CUSTOM DRIVERS ON YOCTO

**NOTE**: following are the files that have been updated by arunima.
[Yesterday 7:14 PM] Arunima
- [ ] kernel-source-new/arch/arm64/boot/dts/renesas (*NOTE*: this is to replace the dts files) (not doing this now)
- [x] kernel-source-new/drivers/media/i2c/os02g10.c
- [x] kernel-source-new/drivers/media/platform/isp_ctrl
- [x] kernel-source-new/drivers/media/platform/simple_isp
- [x] kernel-source-new/drivers/media/i2c/Makefile (need to check how i can edit the makefile that is generated here)
- [x] kernel-source-new/include/linux
- [x] kernel-source-new/include/uapi/linux


## Following are the additional files that I need to add in

The following are the files that I want to add in for the camera
- /drivers/media/i2c/os02g10.c
- /drivers/media/i2c/Makefile
- /drivers/media/Kconfig
- /drivers/media/platform/isp_ctrl/isp_ctrl.c
- /drivers/media/platform/isp_ctrl/Makefile
- /drivers/media/platform/simple_isp/Makefile
- /drivers/media/platform/simple_isp/simple_isp_ae.c
- /drivers/media/platform/simple_isp/simple_isp.c
- /drivers/media/platform/simple_isp/simple_isp_config.c
- /drivers/media/platform/simple_isp/simple_isp_gamma.c

- /include/linux/isp_ctrl.h
- /include/linux/renesas-v4l2-controls.h
- /include/linux/rk-camera-module.h
- /include/linux/rk-preisp.h
- /include/linux/rk-video-format.h
- /include/linux/rzg2l_isp_ctrl.h
- /include/linux/simple_isp_ae.h
- /include/linux/simple_isp_config.h
- /include/linux/simple_isp_default.h
- /include/linux/simple_isp_gamma.h

- /include/uapi/linux/renesas-v4l2-controls.h
- /include/uapi/linux/v4l2-controls.h (changed for no reason looks like)
- /include/uapi/linux/videodev2.h (changed for no reason looks like) (stupid idiots added a non asii char here)

# STEP 1: Adding the camera drivers in

1. Create the custom layer on yocto that will contain all the changes that were made
	- `bitbake-layers create-layers pirllabs-os02g10-camera-drivers`
2. Edit the conf layer.conf file in the new layer to include the following canges 

*NOTE*: need to understand more about this configuration 
```conf
# Layer configuration
BBFILE_COLLECTIONS += "custom"
BBFILE_PATTERN_custom = "^${LAYERDIR}/"
BBFILE_PRIORITY_custom = "6"
LAYERSERIES_COMPAT_custom = "warrior"
```

3. add the following dir path to make sure that we follow tha same structure used for the kernel in the rest of the distribution
	- `mkdir -p recipes-kernel/linux/linux-renesas`
4. create the bb file in for the drivers. this file will define how to build and install the drivers

- [ ] Need to find out what it the correct license i need to provide here.

```os02g10-camera-drivers.bb
SUMMARY = 'Camera drivers for pirllabs os02g10 MIPI camera'
LICENSE = 'GPLv2'

SRC_URI = "file://i2c/os02g10.c \
		   file://i2c/Makefile	\
           file://platform/isp_ctrl/isp_ctrl.c \
           file://platform/isp_ctrl/Makefile \
           file://platform/simple_isp/Makefile \
           file://platform/simple_isp/simple_isp_ae.c \
           file://platform/simple_isp/simple_isp.c \
           file://platform/simple_isp/simple_isp_config.c \
	       file://platform/simple_isp/simple_isp_gamma.c"

S = "${WORKDIR}" 

# Specify where the kernel sources are located
KERNEL_SRC = "${STAGING_KERNEL_DIR}"

COMPATIBLE_MACHINE = "smarc-rzv2l smarc-rzg2l"
```

### Explanation of what the above file does
```os02g10-camera-drivers.bb
SUMMARY = 'Camera drivers for pirllabs os02g10 MIPI camera'
LICENSE = 'GPLv2'

SRC_URI = "file://i2c/os02g10.c \
		   file://i2c/Makefile	\
           file://platform/isp_ctrl/isp_ctrl.c \
           file://platform/isp_ctrl/Makefile \
           file://platform/simple_isp/Makefile \
           file://platform/simple_isp/simple_isp_ae.c \
           file://platform/simple_isp/simple_isp.c \
           file://platform/simple_isp/simple_isp_config.c \
	       file://platform/simple_isp/simple_isp_gamma.c"

S = "${WORKDIR}" 

# Specify where the kernel sources are located
KERNEL_SRC = "${STAGING_KERNEL_DIR}"

COMPATIBLE_MACHINE = "smarc-rzv2l smarc-rzg2l"
```

*NOTE*: an additional thing that i could try here is the ability check sums on the URI once the package is moved out of developmnet phase

5. Make sure that every change i make here has a Makefile that is associated with it 
6. Create a file linux-renesas_5.10.bbappend to apply the changes to the existing kernel recipe

**Following shows the location that i need to place the file**

```bash
meta-custom-layer/
|-- recipes-kernel/
	|-- linux/
		`-- linux-renesas_5.10.bbappend
```

```.bbappend
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://i2c/os02g10.c \
		   file://i2c/Makefile	\
           file://platform/isp_ctrl/isp_ctrl.c \
           file://platform/isp_ctrl/Makefile \
           file://platform/simple_isp/Makefile \
           file://platform/simple_isp/simple_isp_ae.c \
           file://platform/simple_isp/simple_isp.c \
           file://platform/simple_isp/simple_isp_config.c \
	       file://platform/simple_isp/simple_isp_gamma.c"

# Ensure the kernel configuration includes your drivers
KERNEL_FEATURES_append = " \
    ${THISDIR}/files/config"
```

7. updated bblayes.conf with your new layer (use the bitbake-layers add-layer <name-of-layer>)
8. Add your layer to the local.conf
	- ensure that your custom kernel recipe is being used

```
PREFERRED_PROVIDER_virtual/kernel = "linux-renesas"
```
**NOTE**: not sure if this is required please have a look into this when i am building 
9. build the image using the `bitbake core-image-minimal`


## Overall structre of the custom driver layer

```bash
|-- pirllabs-os02g10-camera-driver\
	|-- conf\
	|	`-- layers.conf
	|-- recipes-kernel\
		|-- linux\
			|-- linux-renesas_5.10.bbappend
			|-- linux-renesas\
				|-- os02g10-camera-drivers.bb
				|-- files\
					|-- i2c\
					|	|-- os02g10.c 
					|	`-- Makefile
					|-- platform\
						|-- isp_ctrl\
						|	|-- isp_ctrl.c 
						|	`-- Makefile
						|-- simple_isp\
							|-- simple_isp_ae.c 
							|-- simple_isp.c 
							|-- simple_isp_config.c 
							|-- simple_isp_gamma.c 
							`-- Makefile
```

## Updating camera drivers 

### Makefile additions and changes

```Makefile
obj-y += driver_name.o
``` 

### Update the Kconfig (if necessary)

the below is the sample addition that was done for the other renesas camera

```Kconfig
config VIDEO_OV5645_RAW10
	tristate "OmniVision OV5645 sensor RAW10 support"
	depends on OF
	depends on I2C && VIDEO_V4L2
	select MEDIA_CONTROLLER
	select VIDEO_V4L2_SUBDEV_API
	select V4L2_FWNODE
	help
	  This is a Video4Linux2 sensor driver for the OmniVision
	  OV5645 camera.

	  To compile this driver as a module, choose M here: the
	  module will be called ov5645.

config VIDEO_OV5645_RAW8
	tristate "OmniVision OV5645 sensor RAW8 support"
	depends on OF
	depends on I2C && VIDEO_V4L2
	select MEDIA_CONTROLLER
	select VIDEO_V4L2_SUBDEV_API
	select V4L2_FWNODE
	help
	  This is a Video4Linux2 sensor driver for the OmniVision
	  OV5645 camera.

	  To compile this driver as a module, choose M here: the
	  module will be called ov5645.
```

*NOTE*: here the config i set for the tristate is what i can ahave as the option for the Makefile instead of using `obj-y` hardcoded
