package bg.tusofia.fktt.zkalev.dessertation;



import android.app.AlertDialog;
import android.content.Context;


public class AlertDialogManager {

	

	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		if(status != null)
			// Setting alert dialog icon
			alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

	
		alertDialog.show();
	}
}
