#!/bin/sh

PHON_OPTS="-Dphon.debug=true -Dca.phon.syllabifier.basic.BasicSyllabifier.trackStages=true"

JAVA_HOME=`/usr/libexec/java_home -v1.6` \
java -Xms250m -Xmx1024m \
-Xdock:name=Phon \
-Xdock:icon=app/src/main/resources/data/icons/Phon.icns \
-cp "app/target/phon-app-1.7.0-SNAPSHOT.jar:app/target/deps/*" \
-Dswing.aatext=true \
-Dcom.apple.mrj.application.apple.menu.about.name=Phon \
-Dcom.apple.macos.smallTabs=true \
-Dapple.laf.useScreenMenuBar=true \
-Dcom.apple.mrj.application.live-resize=true \
-Dapple.awt.textantialiasing=on \
-Dapple.awt.showGrowBox=true \
-Dfile.encoding=UTF-8 \
-Dsun.jnu.encoding=UTF-8 \
-Dapple.laf.useScreenMenuBar=true \
-Dapple.awt.graphics.UseQuartz=true \
$PHON_OPTS \
ca.phon.app.Main 2>&1 | tee phon.log
