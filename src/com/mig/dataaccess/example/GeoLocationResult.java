package com.mig.dataaccess.example;

import java.util.List;

public class GeoLocationResult {
	
	private String status;
	private List<GeoLocationResultEntry> results;
	
	public String getStatus() { return status; }
	public List<GeoLocationResultEntry> getResults() { return results; }

	public void setStatus(String status) { this.status = status; }
	public void setResults(List<GeoLocationResultEntry> results) { this.results = results; }

	
	public class GeoLocationResultEntry {
		
		private List<AddressComponent> address_components;
		private String formatted_address;
		private AddressGeometry geometry;
		private List<String> types; 
		
		public List<AddressComponent> getAddress_components() { return address_components; }
		public String getFormatted_address() { return formatted_address; }
		public AddressGeometry getGeometry() { return geometry; }
		public List<String> getTypes() { return types; }
		
		public void setAddress_components(List<AddressComponent> address_components) { this.address_components = address_components; }
		public void setFormatted_address(String formatted_address) { this.formatted_address = formatted_address; }
		public void setGeometry(AddressGeometry geometry) { this.geometry = geometry; }
		public void setTypes(List<String> types) { this.types = types; }
	}
	
	public class AddressComponent  {
		
		private String long_name;
		private String short_name;
		private List<String> types;
		
		public String getLong_name() { return long_name; }
		public String getShort_name() { return short_name; }
		public List<String> getTypes() { return types; }
		
		public void setLong_name(String long_name) { this.long_name = long_name; }
		public void setShort_name(String short_name) { this.short_name = short_name; }
		public void setTypes(List<String> types) { this.types = types; }
	}
	
	public class AddressGeometry {
		
		private AddressGeometryLocation location;
		private AddressGeometryViewport viewport;
		private String location_type;
		
		public AddressGeometryLocation getLocation() { return location; }
		public AddressGeometryViewport getViewport() { return viewport; }
		public String getLocation_type() { return location_type; }
		
		public void setLocation(AddressGeometryLocation location) { this.location = location; }
		public void setViewport(AddressGeometryViewport viewport) { this.viewport = viewport; }
		public void setLocation_type(String location_type) { this.location_type = location_type; }
	}
	
	public class AddressGeometryLocation {
		
		private double lat;
		private double lng;
		
		public double getLat() { return lat; }
		public double getLng() { return lng; }
		
		public void setLat(double lat) { this.lat = lat; }
		public void setLng(double lng) { this.lng = lng; }
	}

	public class AddressGeometryViewport {
		
		private AddressGeometryLocation northeast;
		private AddressGeometryLocation southwest;
		
		public AddressGeometryLocation getNortheast() { return northeast; }
		public AddressGeometryLocation getSouthwest() { return southwest; }
		
		public void setNortheast(AddressGeometryLocation northeast) { this.northeast = northeast; }
		public void setSouthwest(AddressGeometryLocation southwest) { this.southwest = southwest; }
	}
	
}
