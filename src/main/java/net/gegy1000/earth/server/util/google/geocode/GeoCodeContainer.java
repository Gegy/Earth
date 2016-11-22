package net.gegy1000.earth.server.util.google.geocode;

public class GeoCodeContainer {
    public String status;
    public Result[] results;

    public class Result {
        public String[] types;
        public String formatted_address;
        public AddressComponent[] address_components;
        public Geometry geometry;
        public String place_id;
    }

    public class Geometry {
        public Location location;
        public String location_type;
        public Viewport viewport;
    }

    public class Viewport {
        public Location southwest;
        public Location northeast;
        public Location southeast;
        public Location northwest;
        public Location south;
        public Location east;
        public Location north;
        public Location west;
    }

    public class Location {
        public double lat;
        public double lng;
    }

    public class AddressComponent {
        public String long_name;
        public String short_name;
        public String[] types;
    }
}
