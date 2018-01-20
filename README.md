# CarCam School Project 2017

The CarCam is a small RC car with powered by a raspberry pi. There is a camera embedded. The aim of this project is to secure a site with moving from the security post thanks to the CarCam, an android smartphone and wifi.

This car can go almost everywhere. It can scan car licence plates.

Check out the video.

All the equipments cost about 150 euros.

We bought a kit at https://www.robotshop.com/eu/fr/voiture-intelligente-video-pour-raspberry-pi-b.html .

###################################################################################################################################

Here, you have the server program in python (in the raspberry pi) and the android app code.

Both programs work. You may modify the code them in order to make them work.

Be sure that every wire is correctly connect with boards otherwise the server program may not works.

In the raspberry pi, you have to install openalpr and build a database.

Here it is how you can install openalpr with raspbian :

  sudo apt-get update
  sudo apt-get install liblog4cplus-1.0-4 beanstalkd libgtk2.0-0 libtiff5 libdc1394-22 libavcodec-dev libavformat-dev libswscale-dev libv4l-dev libxvidcore-dev libx264-dev
  wget http://www.rgot.org/wp-content/uploads/2016/12/openalpr.tgz
  tar -zxvf openalpr.tgz
  sudo dpkg -i openalpr_20161219-1-complet_armhf.deb
  rm openalpr_20161219-1-complet_armhf.deb
  rm openalpr.tgz
  
(http://www.rgot.org/openalpr-sur-raspberry/)

