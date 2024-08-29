1# CUSTOM DRIVERS ON YOCTO

**NOTE**: following are the files that have been updated by arunima.
[Yesterday 7:14 PM] Arunima
- [ ] kernel-source-new/arch/arm64/boot/dts/renesas (*NOTE*: this is to replace the dts files) (not doing this now)
- [x] kernel-source-new/drivers/media/i2c/$$os02g10.c
- [x] kernel-source-new/drivers/media/platform/isp_ctrl
- [x] kernel-source-new/drivers/media/platform/simple_isp
- [x] kernel-source-new/drivers/media/i2c/Makefile (need to check how i can edit the makefile that is generated here)
- [x] kernel-source-new/include/linux
- [x] kernel-source-new/include/uapi/linux


## Following are the additional files that I need to add in

The following are the files that I want to add in for the camera
- /drivers/media/i2c/os02g10.c
- /drivers/media/i2c/Makefile
- /drivers/media/i2c/Kconfig
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

SRC_URI = "file://drivers/media/i2c/os02g10.c \
		   file://drivers/media/i2c/Makefile \
		   file://drivers/media/i2c/Kconfig \
           file://drivers/media/platform/isp_ctrl/isp_ctrl.c \
           file://drivers/media/platform/isp_ctrl/Makefile \
           file://drivers/media/platform/simple_isp/Makefile \
           file://drivers/media/platform/simple_isp/simple_isp_ae.c \
           file://drivers/media/platform/simple_isp/simple_isp.c \
           file://drivers/media/platform/simple_isp/simple_isp_config.c \
	       file://drivers/media/platform/simple_isp/simple_isp_gamma.c"

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
	`-- recipes-kernel\
		`-- linux\
			|-- linux-renesas_5.10.bbappend
			`-- linux-renesas\
				|-- os02g10-camera-drivers.bb
				`-- files\
					|--drivers\
					|	`--media\
					|		|-- i2c\
					|		|	|-- os02g10.c 
					|		|	|-- Kconfig
					|		|	`-- Makefile
					|		`-- platform\
					|			|-- isp_ctrl\
					|			|	|-- isp_ctrl.c 
					|			|	`-- Makefile
					|			`-- simple_isp\
					|				|-- simple_isp_ae.c 
					|				|-- simple_isp.c 
					|				|-- simple_isp_config.c 
					|				|-- simple_isp_gamma.c 
					|				`-- Makefile
					`-- include\
						|-- linux\
						|	|-- isp_ctrl.h 
						|	|-- renesas-v42l-controls.h 
						|	|-- rk-camera-module.h 
						|	|-- rk-preisp.h 
						|	|-- rk-video-format.h 
						|	|-- rzg2l_isp_ctrl.h 
						|	|-- simple_isp_ae.h 
						|	|-- simple_isp_config.h 
						|	|-- simple_isp_default.h 
						|	`-- simple_isp_gamma.h 
						`-- uapi\
							`-- linux\
								|-- renesas-v42l-controls.h 
								|-- v4l2-controls.h 
								`-- videodev.h
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


# Considerations

*NOTE*: I think it would be better to split this recipie into different parts or different layers as the simple ISP and isp_ctrl do not seem to have any relation to the actual camera driver component 

it might be an idea to combine the os02g10 camera driver as one layer and the isp control stuff as another layer as they deal with very different thing when we are talking about from a kernel perspective


# File by file understanding of what each file does.

## Camera driver related files

- /drivers/media/i2c/os02g10.c
- /drivers/media/i2c/Makefile
- /drivers/media/i2c/Kconfig

## isp_ctrl related files 

- /drivers/media/platform/isp_ctrl/isp_ctrl.c
- /drivers/media/platform/isp_ctrl/Makefile

## simple_isp realted files 

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

## os02g10 camera drivers

The below files will be added in together as one recepie.

### /drivers/media/i2c/os02g10.c
- Compleatly new file.
- need to have a look at this file last to see if any mistake was made when they were writing this driver 

### /drivers/media/i2c/Makefile
- Existing file 
- will need to change the way the new driver was added in the kernel provided by Renesas
- will have to work together with the Kconfig file
- need to add the new camera driver into this makefile

### /drivers/media/i2c/Kconfig
- Existing file
- The new camera driver was not added into this file at all by renesas will need to add it in referencing how it was also added into the Makefile
- Need to add the new camera driver into this Kconfig file 

## isp_ctrl
The below files will be added into together as one recepie

### /drivers/media/platform/isp_ctrl/Makefile
- Completely new file

### /drivers/media/platform/isp_ctrl/isp_ctrl.c
- Completely new file 

## simple_isp
The below files will be added in together as one recepie, all the below files are completly new and have no previous implementation.

### /drivers/media/platform/simple_isp/Makefile
### /drivers/media/platform/simple_isp/simple_isp.c
### /drivers/media/platform/simple_isp/simple_isp_ae.c
### /drivers/media/platform/simple_isp/simple_isp_config.c
### /drivers/media/platform/simple_isp/simple_isp_gamma.c

## include/linux 
- There is no Makefile here so its looks like i just need to add the files here.
- This is for the kernel-space

### /include/linux/isp_ctrl.h
- I am gussing this is the header file for isp_ctrl.c 
- This is a new file that did not exist before 

### /include/linux/renesas-v4l2-controls.h
- This is a new file that is not there before 
- This file is there in both `/include/linux` and ``/include/uapi/linux/` and there are minor differences 
- Compared to the file in the directory `/include/uapi/linux` this file looks like the one that was edited and updated while the other one was not.
- This also looks like it has a encoding error as well with a non-ascii char.
- I am not sure i need to add this can i get away with not having this., this also looks be a small enough file that has no relation to the camera driver.

### /include/linux/rk-camera-module.h
- This is a new file that is not there before.
- I am sure that this is referenced in the camera driver

### /include/linux/rk-preisp.h
- This is another file that is referenced in the main camera driver module

### /include/linux/rk-video-format.h
- This is being called by rk-camera-module.h
- This is a new file that has been added into the kernel

### /include/linux/rzg2l_isp_ctrl.h

I am guessing the below is the header files for simple_isp
The below are all new files that did not exist before 
### /include/linux/simple_isp_ae.h
### /include/linux/simple_isp_default.h
### /include/linux/simple_isp_config.h
### /include/linux/simple_isp_gamma.h


## /include/uapi/linux (DONE)
- This is for then user-space

### /include/uapi/linux/renesas-v4l2-controls.h
- This is a new file
- Ask why this file is not updated compared to `\include\linux`.

### /include/uapi/linux/v4l2-controls.h
- This is an existing file that looks like there is hardly any change to it (and the change that is there looks like it is an encoding error i want to make sure that i am not touching this file when i am doing my first try)
- This is being used by the camera driver os02g10.c

### /include/uapi/linux/videodev2.h
- This is an existing file that looks like there is hardly any change to it (and the change that is there looks like it is an encoding error i want to make sure that i am not touching this file when i am doing my first try)
TODO: need to look if the files in `include/linux/` and `/include/uapi/linux` are different or are they the same file for `reneasas-v4l2-controls.h`
	NOTE: both the files are almost the same except for some random differences it more looks like some changes were made in the kernel-space and then not made in the user-space, will need to look into this.

# Question to ask on Monday 

- In the path /drivers/media/i2c/app_isp_monitoring -> look like this file was added additionally but have not been mentioned as additions
- In the path /drivers/media/i2c/app_usbcam_http -> looks like this file was added additionally but have not been mentionaed as additions 
- Look for the file differences that have been found in the `/include/linux/renesas-v4l2-controls.h` and `/include/uapi/linux/renesas-v4l2-controls.h`

## Overall structure if the custom layer (updated multiple recipes)

```
|-- pirllabs-os02g10-camera-driver\
	|-- conf\
	|	`-- layers.conf
	|-- recipes-kernel\
		|-- linux\
			|-- linux-renesas_5.10.bbappend
			|-- linux-renesas\
				|-- files\
		        |	|-- drivers\
				|	|-- media\
				|	|	|-- i2c\
				|	|	|	|-- os02g10.c
				|	|	|	|-- Kconfig
				|	|	|	`-- Makefile
				|	|	|-- platform\
				|	|		|-- isp_ctrl\
				|	|		|	|-- isp_ctrl.c
				|	|		|	|-- Makefile
				|	|		|-- simple_isp\
				|	|			|-- simple_isp.c
				|	|			|-- simple_isp_config.c
				|	|			|-- simple_isp_gamma.c
				|	|			`-- simple_isp_ae.c
				|	|-- include\
				|			|-- linux\
				|		    |	|--	isp_ctrl.h
				|		    |	|-- renesas-v4l2-controls.h
				|		    |	|-- rk-camera-module.h
				|		    |	|-- rk-preisp.h
				|		    |	|-- rk-video-format.h
				|		    |	`-- rzg2l_isp_ctrl.h (have to look into this file)
				|			|-- uapi\
				|				|-- linux\
				|					`-- renesas-v4l2-controls.h (Need to look why file in kernel space and in user space are different)
				|-- os02g10-drivers\
				|	`-- pirllabs-os02g10-camera-drivers.bb
				|-- isp_ctrl\
				|	`-- pirllabs-rzv2l-isp_ctrl.bb
				|-- simple_isp\
				|	`-- prilllabs-rzv2l-simple-isp.bb
				|-- camera-header-files\
				|	`-- pirllabs-rzv2l-camera-header-files.bb
				|-- kernel-user-space-header-files\
				|	`-- prillabs-kernel-user-space-header-files.bb
				|-- kernel-space-header-files\
				|	`-- pirllabs-kernel-space-header-files.bb
```	