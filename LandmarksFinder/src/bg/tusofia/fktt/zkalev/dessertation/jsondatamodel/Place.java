package bg.tusofia.fktt.zkalev.dessertation.jsondatamodel;

import java.io.Serializable;

import com.google.api.client.util.Key;


public class Place implements Serializable
{
    private static final long serialVersionUID = -5753876947130986678L;

    @Key
    public String id;

    @Key
    public String name;

    @Key
    public String reference;

    @Key
    public String icon;

    @Key
    public String vicinity;

    @Key
    public Geometry geometry;

    @Key
    public String formatted_address;

    @Key
    public String formatted_phone_number;

    @Key
    public String opening_hours;

    @Key
    public Photos[] photos;

    @Override
    public String toString()
    {
        return name + " - " + id + " - " + reference + "---" + opening_hours;
    }

    public static class Geometry implements Serializable
    {
        private static final long serialVersionUID = -9067478066432947576L;
        @Key
        public Location location;
    }

    public static class Location implements Serializable
    {

        private static final long serialVersionUID = 3572181220104323450L;

        @Key
        public double lat;

        @Key
        public double lng;
    }

    public static class Photos implements Serializable
    {
        private static final long serialVersionUID = -821269026166908162L;

        @Key
        int height;

        @Key
        public String photo_reference;

        @Key
        int width;
    }

}
