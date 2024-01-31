package ca.phon.app.session;

public enum ViewPosition {
    WORK(25, 0, 50, 100, "work"),
    LEFT_TOP(0, 0, 25, 50, "left-top"),
    LEFT_BOTTOM(0, 50, 25, 50, "left-bottom"),
    RIGHT_TOP(75, 0, 25, 50, "right-top"),
    RIGHT_BOTTOM(75, 50, 25, 50, "right-bottom"),
    BOTTOM_LEFT(0, 100, 50, 50, "bottom-left"),
    BOTTOM_RIGHT(50, 100, 50, 50, "bottom-right");

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
