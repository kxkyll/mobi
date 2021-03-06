/*
 * Portions of this file are modifications based on work created and shared by
 * Dunni, gjpc, gregm and speedplane at StackOverflow[1]. To the best of our
 * knowledge[2], this code has been placed in the public domain.
 *
 * [1]: http://stackoverflow.com/questions/2617266/how-to-adjust-text-font-size-to-fit-textview
 * [2]: http://meta.stackoverflow.com/a/25957
 */
package fi.aalto.cs.mss.mylocationapp;

        import android.content.Context;
        import android.graphics.Paint;
        import android.util.AttributeSet;
        import android.util.TypedValue;
        import android.widget.TextView;

public class MyWidthAdjustingTextView extends TextView {
    private Paint mTestPaint;

    public MyWidthAdjustingTextView(Context context) {
        super(context);
        initialise();
    }

    public MyWidthAdjustingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before,
                                 final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        /*
         * Maximum size defaults to the initially specified text size unless it
         * is too small
         */
    }

    /**
     * Resize the font so the specified text fits in the text box assuming the
     * text box is of specified width.
     */
    private void refitText(String text, int textWidth) {
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = 100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while ((hi - lo) > threshold) {
            float size = (hi + lo) / 2;
            mTestPaint.setTextSize(size);
            if (mTestPaint.measureText(text) >= targetWidth)
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

}



