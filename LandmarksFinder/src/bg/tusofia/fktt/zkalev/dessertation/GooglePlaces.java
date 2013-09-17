package bg.tusofia.fktt.zkalev.dessertation;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.PlaceDetails;
import bg.tusofia.fktt.zkalev.dessertation.jsondatamodel.PlacesList;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

public class GooglePlaces {

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	/** Google API Key */
	private static final String API_KEY = "";
	/** Google Places serach url's */
	private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	private static final String PLACES_FOTOS_URL = "https://maps.googleapis.com/maps/api/place/photo?";

	/**
	 * Searching places
	 * 
	 * @param latitude - latitude of place
	 *            
	 * @params longitude - longitude of place
	 * */
	public PlacesList search(double latitude, double longitude, double radius,
			String types) throws Exception {
		try {
			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("location", latitude + "," + longitude);
			request.getUrl().put("radius", radius);
			request.getUrl().put("sensor", "true");
			request.getUrl().put("language", "bg");
			if (types != null)
				request.getUrl().put("types", types);

			PlacesList list = request.execute().parseAs(PlacesList.class);
			/** Check log cat for places response status */
			Log.d("Places Status", "" + list.status);
			return list;

		} catch (HttpResponseException e) {
			Log.e("Error:", e.getMessage());
			return null;
		}

	}

	
	/**
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public PlaceDetails getPlaceDetails(String reference) throws Exception {
		try {
			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("reference", reference);
			request.getUrl().put("sensor", "true");
			request.getUrl().put("language", "bg");
			String d=request.execute().parseAsString();
			Log.d("LLLLOAAAAA SEARCH PLACES", d);
			PlaceDetails placeDetails = request.execute().parseAs(
					PlaceDetails.class);
			return placeDetails;

		} catch (HttpResponseException e) {
			Log.e("Error in Perform Details", e.getMessage());
			throw e;
		}
	}

	/**
	 * @param photoreference
	 * @return
	 * @throws IOException
	 */
	public Bitmap getPlacePhotos(String photoreference) throws IOException {
		try {
			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_FOTOS_URL));
			request.getUrl().put("maxheight", "800");
			request.getUrl().put("maxwidth", "400");
			request.getUrl().put("sensor", "true");
			request.getUrl().put("photoreference", photoreference);
			request.getUrl().put("key", API_KEY);
			Bitmap photo = BitmapFactory.decodeStream(request.execute()
					.getContent());
			return photo;
		} catch (HttpResponseException e) {
			Log.e("Error in Perform Photos", e.getMessage());
			throw e;
		}
	}

	
	/**
	 * @param transport
	 * @return
	 */
	public static HttpRequestFactory createRequestFactory(
			final HttpTransport transport) {
		return transport.createRequestFactory(new HttpRequestInitializer() {
			public void initialize(HttpRequest request) {
				GoogleHeaders headers = new GoogleHeaders();
				headers.setApplicationName("LandmarksFinder");
				request.setHeaders(headers);
				JsonObjectParser parser = new JsonObjectParser(
						new JacksonFactory());
				request.setParser(parser);
			}
		});
	}

}
