package com.example.recyclerview;

public class Device {
    private String address;
    private String manufacturer;

    public Device(String username) {
        this.address = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
