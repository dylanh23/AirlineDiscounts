package dhare.airlinediscounts;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dylan on 21/12/2014.
 */
public class AirlinesAdapter extends ArrayAdapter<Airline> {
    int screenWidth;
    int portionOfScreen;

    public AirlinesAdapter(Context context, ArrayList<Airline> airlines, int screenWidth, int portionOfScreen) {
        super(context, 0, airlines);
        this.screenWidth = screenWidth;
        this.portionOfScreen = portionOfScreen;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Airline airline = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_airline, parent, false);
        }
        // Lookup view for data population
        TextView airlineName = (TextView) convertView.findViewById(R.id.airlineName);
        ImageView airlineIcon = (ImageView) convertView.findViewById(R.id.airlineIcon);
        //ImageView airlineIcon = (ImageView) findViewById(R.id.airlineIcon);
        // Populate the data into the template view using the data object
        airlineName.setText(airline.name);
        airlineName.setTextColor(getContext().getResources().getColor(R.color.dull_white));
        airlineName.setBackgroundColor(Color.BLACK);
        int resID = getContext().getResources().getIdentifier(airline.iconName, "drawable", "dhare.airlinediscounts");
        airlineIcon.setImageResource(resID);

        //addifiwant: make height of image and text based on height of screen, not necessary

        airlineName.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int mMaxWidth = screenWidth-airlineName.getMeasuredWidth();
        airlineIcon.setMaxWidth(mMaxWidth<portionOfScreen ? mMaxWidth : portionOfScreen);

            /*ViewTreeObserver vto = airlineIcon.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                         public boolean onPreDraw() {
                                             // Remove after the first run so it doesn't fire forever
                                             airlineIcon.getViewTreeObserver().removeOnPreDrawListener(this);
                                             airlineIcon.getLayoutParams().width = Math.round(airlineIcon.getMeasuredWidth() / (airlineIcon.getMeasuredHeight() / 30));
                                             airlineIcon.getLayoutParams().height = 30;
                                             int n = airlineIcon.getMeasuredHeight();
                                             return true;
                                         }
                                     });
                airlineIcon.requestLayout();*/
        // Return the completed view to render on screen
        return convertView;
    }
}