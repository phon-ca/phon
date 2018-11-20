#!/bin/sh

PHON_VERSION=$("mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec")
PHON_OPTS="-Dphon.debug=true"

java -Xms250m -Xmx2048m \
-Xdock:name=Phon \
-Xdock:icon=app/src/main/resources/data/icons/Phon.icns \
-cp "app/target/phon-app-$PHON_VERSION.jar:app/target/deps/*" \
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

