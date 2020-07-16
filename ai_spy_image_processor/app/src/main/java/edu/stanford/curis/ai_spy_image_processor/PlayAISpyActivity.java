package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayAISpyActivity extends BasicFunctionality {

    private AISpyImage aiSpyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_spy_game);

        this.aiSpyImage = AISpyImage.getInstance();

        setISpyImage();
        setISpyClue();

    }

    private void setISpyImage(){
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapFactory.decodeFile(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }

    private void setISpyClue(){
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        iSpyClueView.setText(aiSpyImage.generateRandomISpyClue());
    }


}
