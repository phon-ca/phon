#!/bin/sh

java -Xms250m -Xmx1024m -Xdock:name=Phon -Xdock:icon=app/src/main/resources/data/icons/Phon.icns -cp "app/target/phon-app-1.7.0-SNAPSHOT.jar:app/target/deps/*" -Dswing.aatext=true -Dcom.apple.mrj.application.apple.menu.about.name=Phon -Dcom.apple.macos.smallTabs=truei \
-Dapple.laf.useScreenMenuBar=true \
-Dcom.apple.mrj.application.live-resize=true \
-Dapple.awt.textantialiasing=on \
-Dapple.awt.graphics.UseQuartz=true \
-Dapple.awt.showGrowBox=true \
-Dfile.encoding=UTF-8 \
-Dsun.jnu.encoding=UTF-8 \
-Dapple.laf.useScreenMenuBar=true \
ca.phon.app.Main 2>&1 | tee phon.log

