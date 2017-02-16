package com.teramatrix.fleetiq.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.JLatex.core.AjLatexMath;
import com.teramatrix.fleetiq.JLatex.core.Insets;
import com.teramatrix.fleetiq.JLatex.core.TeXConstants;
import com.teramatrix.fleetiq.JLatex.core.TeXFormula;
import com.teramatrix.fleetiq.JLatex.core.TeXIcon;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class CarScoreFragment extends Fragment {


    private View convertView;
    private float mTextSize = 16;
    private String screen;
    private static String mExample2 = "\\begin{array}{l}"
            + "\\forall\\varepsilon\\in\\mathbb{R}_+^*\\ \\exists\\eta>0\\ |x-x_0|\\leq\\eta\\Longrightarrow|f(x)-f(x_0)|\\leq\\varepsilon\\\\"
            + "\\det\\begin{bmatrix}a_{11}&a_{12}&\\cdots&a_{1n}\\\\a_{21}&\\ddots&&\\vdots\\\\\\vdots&&\\ddots&\\vdots\\\\a_{n1}&\\cdots&\\cdots&a_{nn}\\end{bmatrix}\\overset{\\mathrm{def}}{=}\\sum_{\\sigma\\in\\mathfrak{S}_n}\\varepsilon(\\sigma)\\prod_{k=1}^n a_{k\\sigma(k)}\\\\"
            + "\\sideset{_\\alpha^\\beta}{_\\gamma^\\delta}{\\begin{pmatrix}a&b\\\\c&d\\end{pmatrix}}\\\\"
            + "\\int_0^\\infty{x^{2n} e^{-a x^2}\\,dx} = \\frac{2n-1}{2a} \\int_0^\\infty{x^{2(n-1)} e^{-a x^2}\\,dx} = \\frac{(2n-1)!!}{2^{n+1}} \\sqrt{\\frac{\\pi}{a^{2n+1}}}\\\\"
            + "\\int_a^b{f(x)\\,dx} = (b - a) \\sum\\limits_{n = 1}^\\infty  {\\sum\\limits_{m = 1}^{2^n  - 1} {\\left( { - 1} \\right)^{m + 1} } } 2^{ - n} f(a + m\\left( {b - a} \\right)2^{-n} )\\\\"
            + "\\int_{-\\pi}^{\\pi} \\sin(\\alpha x) \\sin^n(\\beta x) dx = \\textstyle{\\left \\{ \\begin{array}{cc} (-1)^{(n+1)/2} (-1)^m \\frac{2 \\pi}{2^n} \\binom{n}{m} & n \\mbox{ odd},\\ \\alpha = \\beta (2m-n) \\\\ 0 & \\mbox{otherwise} \\\\ \\end{array} \\right .}\\\\"
            + "L = \\int_a^b \\sqrt{ \\left|\\sum_{i,j=1}^ng_{ij}(\\gamma(t))\\left(\\frac{d}{dt}x^i\\circ\\gamma(t)\\right)\\left(\\frac{d}{dt}x^j\\circ\\gamma(t)\\right)\\right|}\\,dt\\\\"
            + "\\begin{array}{rl} s &= \\int_a^b\\left\\|\\frac{d}{dt}\\vec{r}\\,(u(t),v(t))\\right\\|\\,dt \\\\ &= \\int_a^b \\sqrt{u'(t)^2\\,\\vec{r}_u\\cdot\\vec{r}_u + 2u'(t)v'(t)\\, \\vec{r}_u\\cdot\\vec{r}_v+ v'(t)^2\\,\\vec{r}_v\\cdot\\vec{r}_v}\\,\\,\\, dt. \\end{array}\\\\"
            + "\\end{array}";

    private String latex ="\n" +
            "\\\\\n" +
            "\\\\\n" +
            "\\large P_{i} = \\normalsize i^{th} \\: Parameter\\\\\n" +
            "\\large \\alpha_{i} = \\normalsize Weightage \\: of \\: i^{th} \\: parameter \\\\\n" +
            "\\large n  = \\normalsize total \\: number \\: of \\: parameters \\\\\n" +
            "\\Delta = \\normalsize Final \\: Vehicle \\: Score \\\\\n" +
            "\\large \\rho_{i} = \\normalsize Points \\: \\: assigned \\\\\n" +
            "\n" +
            " \n" +
            "By\\: using\\: the\\: above\\: Symbols \\:we \\:derived\\: the \\:following \\:equation \\\\\n" +
            "\n" +
            "\\LARGE  \\Delta = \\sum_0^n \\frac {P_{i}\\rho_{i}}{10}\\dotso\\dotso\\dotso\\dotso\\dotso\\dotso\\dotso \\small(1)\\\\\n" +
            "\n" +
            "Where: \\\\\n" +
            "\\quad 0 \\:< \\: \\sum_0^n P_{i}\\rho_{i} \\: < \\: 100 \\\\\n" +
            "\\quad 0 \\:< \\: \\Delta \\: < \\: 10 \\\\\n" +
            "\\quad P_{i}\\rho_{i} = also \\: known \\: as \\: Inclination \\\\\n" +
            "\\quad \\rho_{i} =  Points \\: assigned \\: to \\: the \\: range \\: where \\: parameter \\ value \\: lies \\\\";


    private String paragraph1 ="<h4>How do we calculate score?</h4><p>Flint calculates the car score every minute from the packets recieved from the vehicle , The parameters recieved are fed in the Score Calculator which in realtime calculates the score of the vehicle</p>" +
            "<ul>" +
            "The following 3 procees are followed recursively:" +"<br />"+
            "<li>1.Get the parameter required to calculate score from the packet</li>" +"<br />"+
            "<li>2.Apply the Maths on the parameters and take out the average</li>" +"<br />"+
            "<li>3.Show the score on scale of 10</li>" +"<br />"+
            "</ul>" +
            "<h4>What are the parameters used?</h4>We have handpicked few parameters that impacts the car performance and durablity the most.The weightage of the parameteres is decided so increase or decrease its effect on it <bt />.<br /> " +
            "Parameters are:<br />";


    private String car_score_parameters = "1.Battery voltage<br />" +
            "2.Engine speed<br />"+
            "3.Throttle opening width <br />" +
            "4.Instantaneous fuel Consumption<br />" +
            "5.Single fuel consumption<br />" +
            "6.Total mileage<br />" +
            "7.Current Error code numbers<br />" +
            "8.Coolant temperature<br />" +
            "9.Engine load <br />" +
            "10.Average fuel consumption<br />" +
            "11.Harsh acceleration No.<br />" +
            "12.Driving range <br />" +
            "13.Total fuel consumption volume <br />" +
            "14.Harsh brake No.<br />";

    private String fule_parameters ="1.Instantaneous fuel consumption <br />" +
            "2.Single fuel consumption<br />" +
            "3.Average fuel consumption<br />" +
            "4.Total Fuel consumption<br />";



    private String getParagraph2="<h4>What formula(s) have we used to calculate the score ?</h4> For the math part of it we check in what range the corresponding parameter lie and then assign a point accordingly and then multiply that point with weightage to give an inclination towards that parameter .then a summation of all the parameters inclinations is done which in turn results in a car score with range from zero to hundred which is then divisible by ten to show on 10 point screen.";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(convertView==null) {
            convertView = inflater.inflate(R.layout.carscor_fragment, null, false);
            screen = getArguments().getString("screen");
        }
        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        try {

            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_graph).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initViews() {
        try {
            GeneralUtilities.setUpActionBar(getActivity(), this, screen + "", null);
            if (screen.equalsIgnoreCase("Fuel Score")) {
                ((TextView) convertView.findViewById(R.id.txt_msg)).setText(Html.fromHtml(paragraph1 + fule_parameters + getParagraph2));
            } else if (screen.equalsIgnoreCase("Vehicle Score")) {
                ((TextView) convertView.findViewById(R.id.txt_msg)).setText(Html.fromHtml(paragraph1 + car_score_parameters + getParagraph2));
            }
            setformula(latex);
        }catch (Exception e)
        {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }
    private void setformula(String mLatex) {
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        TeXFormula formula = new TeXFormula(mLatex);
        TeXIcon icon = formula.new TeXIconBuilder()
                .setStyle(TeXConstants.STYLE_DISPLAY)
                .setSize(mTextSize)
                .setWidth(TeXConstants.UNIT_PIXEL, w, TeXConstants.ALIGN_LEFT)
                .setIsMaxWidth(true)
                .setInterLineSpacing(TeXConstants.UNIT_PIXEL,
                        AjLatexMath.getLeading(mTextSize)).build();
        icon.setInsets(new Insets(5, 5, 5, 5));

        Bitmap image = Bitmap.createBitmap(icon.getIconWidth(), icon.getIconHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas g2 = new Canvas(image);
        g2.drawColor(Color.WHITE);
        icon.paintIcon(g2, 0, 0);

        Bitmap scaleimage = scaleBitmapAndKeepRation(image, h, w);
        ((ImageView) convertView.findViewById(R.id.img_view)).setImageBitmap(scaleimage);
    }

    public Bitmap scaleBitmapAndKeepRation(Bitmap targetBmp,
                                           int reqHeightInPixels, int reqWidthInPixels) {
        Bitmap bitmap = Bitmap.createBitmap(reqWidthInPixels,
                reqHeightInPixels, Bitmap.Config.ARGB_8888);
        Canvas g = new Canvas(bitmap);
        g.drawBitmap(targetBmp, 0, 0, null);
        targetBmp.recycle();
        return bitmap;
    }
}
