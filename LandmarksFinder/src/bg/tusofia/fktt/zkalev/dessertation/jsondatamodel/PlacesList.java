package bg.tusofia.fktt.zkalev.dessertation.jsondatamodel;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;



public class PlacesList implements Serializable
{

    private static final long serialVersionUID = 6095676394674806250L;

    @Key
    public String status;

    @Key
    public List<Place> results;

}
