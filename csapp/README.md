# Build instructions

Lineman and all dependencies are installed via npm, so first install [node.js](http://nodejs.org); next, install lineman:

$ npm install -g lineman

Above is for the first time install, future build process should run following commands from csapp folder :

```
$ npm install
$ lineman build
```

This will install any new dependencies added to the lineman configuration, process all necessary libs and files, and copy the ember app to the static folder.
Once the build is completed successfully, just add content from static folder to the build.

# Maven integration

The above commands (`npm install` and `lineman build`) have been integrated into the pom.xml file for cloudstead-server.
Simply running a `mvn package` will build the static assets for inclusion within the uberjar.