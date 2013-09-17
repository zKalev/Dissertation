package bg.tusofia.fktt.zkalev.dessertation.jsondatamodel;

import java.io.Serializable;

import com.google.api.client.util.Key;


public class PlaceDetails implements Serializable
{

    private static final long serialVersionUID = 3510857967458462758L;

    @Key
    public String status;

    @Key
    public Place result;

    @Override
    public String toString()
    {
        if (result != null)
        {
            return result.toString();
        }
        return super.toString();
    }
}
