package com.lqkj.location.slug.library.view.overView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

import static android.graphics.Typeface.DEFAULT_BOLD;

public class FloatImageText extends View {
    private Bitmap mBitmap;
    private final Rect bitmapFrame = new Rect();
    private final Rect tmp = new Rect();
    private int mTargetDentity = DisplayMetrics.DENSITY_DEFAULT;

    private Paint mPaint ;
    private String mText1;
    private String[] mText2;
    private String mText3;
    private ArrayList<PaintType> paintTypes;
    private final int[] textSizes = new int[2];
    private int textColor= Color.BLACK;
    private int textSize=sp2px(14);

    public FloatImageText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

    }

    public FloatImageText(Context context, AttributeSet attrs) {
        this(context, attrs,0);

    }


    public FloatImageText(Context context) {
        this(context,null);

    }

    private void init() {
        mTargetDentity = getResources().getDisplayMetrics().densityDpi;
        paintTypes = new ArrayList<PaintType>();
        mPaint=new Paint();
        mPaint.setTextSize(textSize);
        mPaint.setColor(Color.parseColor("#333333"));
        mPaint.setAntiAlias(true);
    }

    public int sp2px(int spVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getResources().getDisplayMetrics());
    }
    public int dp2px(int dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int w = 0, h = 0;
        //图片大小
        w += bitmapFrame.width();
        h += bitmapFrame.height();

        //文本宽度
        if(null != mText2 && mText2.length > 0) {
            paintTypes.clear();
            int size = resolveSize(Integer.MAX_VALUE, widthMeasureSpec);
            measureAndSplitText(mPaint, mText1, size-sp2px(12),mText2,mText3);
            final int textWidth = textSizes[0], textHeight = textSizes[1];
            w += textWidth; //内容宽度
            if(h < textHeight) { //内容高度
                h = (int) textHeight;
            }
        }

        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        setMeasuredDimension(
                resolveSize(w, widthMeasureSpec),
                resolveSize(h+10, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //绘制图片
        if(null != mBitmap) {
            canvas.drawBitmap(mBitmap, null, bitmapFrame, null);
        }
        //绘制文本
        TextLine line;
        final int size = paintTypes.size();
        for(int i = 0; i < size; i++) {
            line = paintTypes.get(i).getTextLine();
            if (mPaint.getTextSize()!=paintTypes.get(i).getFontSize()) {
                mPaint.setTextSize(paintTypes.get(i).getFontSize());
                mPaint.setColor(paintTypes.get(i).getColor());
                if (i==0){
                    mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                }else {
                    mPaint.setTypeface(Typeface.DEFAULT);
                }
            }
            if (mPaint.getColor()!=paintTypes.get(i).color) {
                mPaint.setColor(paintTypes.get(i).getColor());
            }
            canvas.drawText(line.text, line.x+sp2px(7), line.y, mPaint);
        }
//        System.out.println(mTextLines);
    }


    public void setImageBitmap(Bitmap bm) {
        setImageBitmap(bm, null);
    }

    public void setImageBitmap(Bitmap bm, int left, int top) {
        setImageBitmap(bm, new Rect(left, top, 0, 0));
    }
    public void setImageBitmapWithSize(Bitmap bm, int left, int top,int imageWidth,int imageHeight) {
        setImageBitmap(bm, new Rect(left, top, 0, 0),imageWidth,imageHeight);
    }

    public void setImageBitmap(Bitmap bm, Rect bitmapFrame) {
        mBitmap = bm;
        computeBitmapSize(bitmapFrame);
        requestLayout();
        invalidate();
    }
    public void setImageBitmap(Bitmap bm, Rect bitmapFrame,int imageWidth,int imageHeight) {
        mBitmap = bm;
        computeBitmapSize(bitmapFrame,imageWidth,imageHeight);
        requestLayout();
        invalidate();
    }

    public void setText(String text1,String[] text2,String text3) {
        mText1 = text1;
        mText2 = text2;
        mText3 = text3;
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        textColor = color;
        mPaint.setColor(color);
        requestLayout();
        invalidate();
    }
    public void setTextSize(int pxSize)
    {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxSize);
    }
    public void setTextSize(int unit, int size)
    {
        switch (unit)
        {
            case TypedValue.COMPLEX_UNIT_PX:
                textSize = size;
                break;
            case TypedValue.COMPLEX_UNIT_DIP:
                textSize = dp2px(size);
                break;
            case TypedValue.COMPLEX_UNIT_SP:
                textSize = sp2px(size);
                break;
        }
        mPaint.setTextSize(textSize);
        requestLayout();
        invalidate();
    }
    private void computeBitmapSize(Rect rect,int imageWidth,int imageHeight) {
        if(null != rect) {
            bitmapFrame.set(rect);
        }
        if(null != mBitmap) {
            if(rect.right == 0 && rect.bottom == 0) {
                final Rect r = bitmapFrame;
                r.set(r.left, r.top,
                        r.left + imageWidth,
                        r.top + imageHeight);

            }
        } else {
            bitmapFrame.setEmpty();
        }
    }
    private void computeBitmapSize(Rect rect) {
        if(null != rect) {
            bitmapFrame.set(rect);
        }
        if(null != mBitmap) {
            if(rect.right == 0 && rect.bottom == 0) {
                final Rect r = bitmapFrame;
                r.set(r.left, r.top,
                        r.left + 87,
                        r.top + 87);
            }
        } else {
            bitmapFrame.setEmpty();
        }
    }

    private void measureAndSplitText(Paint p, String content, int maxWidth, String[] content2s,String content3) {
        p.setTextSize(sp2px(15));
        mPaint.setTextSize(sp2px(15));
        int saveMaxWidth = maxWidth;
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int lineHeight = (int) (fm.bottom - fm.top);

        final Rect r = new Rect(bitmapFrame);
        int length = content.length();
        int start = 0, end = 0, offsetX = 0, offsetY = 0;
        int availWidth = maxWidth;
        TextLine line;
        boolean onFirst = true;
        boolean newLine = true;
        while(start < length) {
            end++;
            if(end == length) { //剩余的不足一行的文本
                if(start <= length - 1) {
                    if(newLine) offsetY += lineHeight;
                    line = new TextLine();
                    line.text = content.substring(start, end);
                    line.x = offsetX;
                    line.y = offsetY;
                    paintTypes.add(new PaintType(line,sp2px(15),Color.parseColor("#333333")));
                }
                break;
            }
            Log.d("gc", "offsetY--------- = " + r.bottom);
            p.getTextBounds(content, start, end, tmp);
            if(onFirst) { //确定每个字符串的坐标
                onFirst = false;
                final int height = lineHeight + offsetY;
                if(r.top >= height) { //顶部可以放下一行文字
                    offsetX = 0;
                    availWidth = maxWidth;
                    newLine = true;
                } else if(newLine && (r.bottom >= height && r.left >= tmp.width())) { //中部左边可以放文字
                    offsetX = 0;
                    availWidth = r.left;
                    newLine = false;
                } else if(r.bottom >= height && maxWidth - r.right >= tmp.width()) { //中部右边
                    offsetX = r.right;
                    availWidth = maxWidth - r.right;
                    newLine = true;
                }else if(r.bottom >= height && maxWidth - r.right < tmp.width()) { //右边写不下
                    offsetX = 0;
                    availWidth = r.left;
                    offsetY += lineHeight;
                    newLine = true;
                }else { //底部
                    offsetX = 0;
                    availWidth = maxWidth;
                    if(offsetY < r.bottom) offsetY = r.bottom;
                    newLine = true;
                }
            }
            Log.d("gc", "offsetY1 = " + offsetY);
            if(tmp.width() > availWidth) { //保存一行能放置的最大字符串
                onFirst = true;
                line = new TextLine();
                line.text = content.substring(start, end - 1);
                line.x = offsetX;
                paintTypes.add(new PaintType(line,sp2px(15),Color.parseColor("#333333")));
                if(newLine) {
                    offsetY += lineHeight;
                    line.y = offsetY;
                    Log.d("gc", "offsetY1 = " + offsetY + " ^^^^^^^^^^^^^lineHeight = " + lineHeight);
                } else {
                    line.y = offsetY + lineHeight;
                    Log.d("gc", "offsetY2 = " + offsetY + " ^^^^^^^^^^^^^lineHeight = " + lineHeight);
                }
                start = end - 1;
            }
        }
        p.setTextSize(sp2px(12));
        mPaint.setTextSize(sp2px(12));
        fm = mPaint.getFontMetrics();
        lineHeight = (int) (fm.bottom - fm.top);
        length = content3.length();
        start = 0;end = 0;
        onFirst = true;
        newLine = true;
        if (TextUtils.isEmpty(content)) {
            offsetY += sp2px(7);
        }
        while(start < length) {
            end++;
            if(end == length) { //剩余的不足一行的文本
                if(start <= length - 1) {
                    if(newLine) offsetY += lineHeight;
                    line = new TextLine();
                    line.text = content3.substring(start, end);
                    line.x = offsetX;
                    line.y = offsetY;
                    paintTypes.add(new PaintType(line,sp2px(12),Color.parseColor("#999999")));
                }
                break;
            }
            Log.d("gc", "offsetY--------- = " + r.bottom);
            p.getTextBounds(content3, start, end, tmp);
            if(onFirst) { //确定每个字符串的坐标
                onFirst = false;
                final int height = lineHeight + offsetY;
                if(r.top >= height) { //顶部可以放下一行文字
                    offsetX = 0;
                    availWidth = maxWidth;
                    newLine = true;
                } else if(newLine && (r.bottom >= height && r.left >= tmp.width())) { //中部左边可以放文字
                    offsetX = 0;
                    availWidth = r.left;
                    newLine = false;
                } else if(r.bottom >= height && maxWidth - r.right >= tmp.width()) { //中部右边
                    offsetX = r.right;
                    availWidth = maxWidth - r.right;
                    newLine = true;
                }else if(r.bottom >= height && maxWidth - r.right < tmp.width()) { //右边写不下
                    offsetX = 0;
                    availWidth = r.left;
                    offsetY += lineHeight;
                    newLine = true;
                }else { //底部
                    offsetX = 0;
                    availWidth = maxWidth;
                    if(offsetY < r.bottom) offsetY = r.bottom;
                    newLine = true;
                }
            }
            Log.d("gc", "offsetY1 = " + offsetY);
            if(tmp.width() > availWidth) { //保存一行能放置的最大字符串
                onFirst = true;
                line = new TextLine();
                line.text = content3.substring(start, end - 1);
                line.x = offsetX;
                paintTypes.add(new PaintType(line,sp2px(12),Color.parseColor("#999999")));
                if(newLine) {
                    offsetY += lineHeight;
                    line.y = offsetY;
                    Log.d("gc", "offsetY1 = " + offsetY + " ^^^^^^^^^^^^^lineHeight = " + lineHeight);
                } else {
                    line.y = offsetY + lineHeight;
                    Log.d("gc", "offsetY2 = " + offsetY + " ^^^^^^^^^^^^^lineHeight = " + lineHeight);
                }
                start = end - 1;
            }
        }
        p.setTextSize(sp2px(12));
        mPaint.setTextSize(sp2px(12));
        fm = mPaint.getFontMetrics();
        if (TextUtils.isEmpty(content)){
            offsetY = 0;
        }else if(TextUtils.isEmpty(content3)) {
            offsetY += sp2px(25);
        }else {
            offsetY += sp2px(15);
        }
        lineHeight = (int) (fm.bottom - fm.top);
        for (int i=0;i<content2s.length;i++){
            String content2 = content2s[i];
            length = content2.length();
            start = 0;end = 0;
            boolean firstLine = true;
            Rect tmpt = new Rect();
            p.getTextBounds("宽", 0, 1, tmpt);
            int textWidth = tmpt.width();
            onFirst = true;
            newLine = true;
            while(start < length) {
                end++;
                if (firstLine){
                    maxWidth = saveMaxWidth - textWidth*2;
                }else {
                    maxWidth = saveMaxWidth;
                }

                if(end == length) { //剩余的不足一行的文本
                    if(start <= length - 1) {
                        if(newLine) offsetY += lineHeight;
                        line = new TextLine();
                        line.text = content2.substring(start, end);
                        line.x = offsetX;
                        line.y = offsetY;
                        if (firstLine){
                            firstLine = false;
                            line.x = line.x + textWidth*2;
                        }
                        paintTypes.add(new PaintType(line,sp2px(12),Color.parseColor("#333333")));
                        if (i==content2s.length-1) {
                            offsetY += lineHeight*1.5;
                            line = new TextLine();
                            line.text = " ";
                            line.x = offsetX;
                            line.y = offsetY;
                            paintTypes.add(new PaintType(line, sp2px(12), Color.parseColor("#333333")));
                        }
                    }
                    break;
                }
                Log.d("gc", "offsetY--------- = " + r.bottom);
                p.getTextBounds(content2, start, end, tmp);
                if(onFirst) { //确定每个字符串的坐标
                    onFirst = false;
                    final int height = lineHeight + offsetY;
                    if(r.top >= height) { //顶部可以放下一行文字
                        offsetX = 0;
                        availWidth = maxWidth;
                        newLine = true;
                    } else if(newLine && (r.bottom >= height && r.left >= tmp.width())) { //中部左边可以放文字
                        offsetX = 0;
                        availWidth = r.left;
                        newLine = false;
                    } else if(r.bottom >= height && maxWidth - r.right >= tmp.width()) { //中部右边
                        offsetX = r.right;
                        availWidth = maxWidth - r.right;
                        newLine = true;
                    }else if(r.bottom >= height && maxWidth - r.right < tmp.width()) { //右边写不下
                        offsetX = 0;
                        availWidth = r.left;
                        offsetY += lineHeight;
                        newLine = true;
                    }else { //底部
                        offsetX = 0;
                        availWidth = maxWidth;
                        if(offsetY < r.bottom) offsetY = r.bottom;
                        newLine = true;
                    }
                }
                Log.d("gc", "offsetY1 = " + offsetY);
                if(tmp.width() > availWidth) { //保存一行能放置的最大字符串
                    onFirst = true;
                    line = new TextLine();
                    line.text = content2.substring(start, end - 1);
                    line.x = offsetX;
                    if (firstLine){
                        firstLine = false;
                        line.x = line.x + textWidth*2;
                    }
                    paintTypes.add(new PaintType(line,sp2px(12),Color.parseColor("#333333")));
                    if(newLine) {
                        offsetY += lineHeight;
                        line.y = offsetY;
                        Log.d("gc", "offsetY1 = " + offsetY + " ^^^^^^^^^^^^^lineHeight = " + lineHeight);
                    } else {
                        line.y = offsetY + lineHeight;
                        Log.d("gc", "offsetY2 = " + offsetY + " ^^^^^^^^^^^^^lineHeight = " + lineHeight);
                    }
                    start = end - 1;
                }
            }
        }
        textSizes[1] = offsetY;
    }

    class TextLine {
        String text;
        int x;
        int y;

        @Override
        public String toString() {
            return "TextLine [text=" + text + ", x=" + x + ", y=" + y + "]";
        }
    }

    class PaintType{
        TextLine textLine;
        int fontSize;
        int color;

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public TextLine getTextLine() {
            return textLine;
        }

        public void setTextLine(TextLine textLine) {
            this.textLine = textLine;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public PaintType(TextLine textLine, int fontSize,int color) {
            this.textLine = textLine;
            this.color = color;
            this.fontSize = fontSize;
        }
    }
}
