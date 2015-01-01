/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package support;

/**
 *
 * @author shamanth
 */
public class Location
{
    public Double latitude;
    public Double longitude;

    public Location(Double lat,Double lng)
    {
        latitude = lat;
        longitude  = lng;
    }

    @Override
    public String toString()
    {
        return "Latitude: "+latitude+" & Longitude: "+longitude;
    }
}
