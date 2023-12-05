package ca.phon.app.session;

public enum ViewPosition {
    WORK(50, 0, 100, 100, "work"),
    LEFT_TOP(0, 0, 33, 50, "left-top"),
    LEFT_BOTTOM(0, 50, 33, 50, "left-bottom"),
    RIGHT_TOP(133, 0, 33, 50, "right-top"),
    RIGHT_BOTTOM(133, 50, 33, 100, "right-bottom"),
    BOTTOM_LEFT(0, 100, 67, 50, "bottom-left"),
    BOTTOM_RIGHT(67, 100, 66, 50, "bottom-right");

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String name;

    private ViewPosition(int x, int y, int width, int height, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getName() {
        return this.name;
    }

    public static ViewPosition fromName(String name) {
        ViewPosition retVal = null;

        for(ViewPosition vp:ViewPosition.values()) {
            if(vp.getName().equals(name)) {
                retVal = vp;
                break;
            }
        }

        return retVal;
    }

}
