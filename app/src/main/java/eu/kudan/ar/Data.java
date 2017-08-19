package eu.kudan.ar;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import eu.kudan.kudan.ARArbiTrack;
import eu.kudan.kudan.ARNode;

class Data {

    private Quaternion orientation;
    private Vector3f position;
    private Vector3f scale;
    private double longitude;
    private double latitude;

    public Data() {
        latitude = 0.0;
        longitude = 0.0;
        position = new Vector3f(0,0,0);
        scale = new Vector3f(0,0,0);
        orientation = new Quaternion(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public Data(double latitude, double longitude, Vector3f position, Vector3f scale, Quaternion orientation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.position = position;
        this.scale = scale;
        this.orientation = orientation;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public void setOrientation(Quaternion orientation) {
        this.orientation = orientation;
    }

    double getLatitude() {
        return latitude;
    }

    double getLongitude() {
        return longitude;
    }

    Vector3f getPosition() {
        return  position;
    }

    Vector3f getScale() {
        return scale;
    }

    Quaternion getOrientation() {
        return orientation;
    }
}
