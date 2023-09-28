package ca.phon.session.tierdata;

import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.orthography.InternalMedia;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TierDataParserListener extends TierDataBaseListener {

    final List<TierElement> elementList = new ArrayList<>();

    @Override
    public void exitWord(TierDataParser.WordContext ctx) {
        elementList.add(new TierString(ctx.getText()));
    }

    @Override
    public void exitComment(TierDataParser.CommentContext ctx) {
        String commentText = ctx.getText();
        // we may need to recover from a missing end bracket
        if(ctx.END_COMMENT() != null) {
            commentText = commentText.substring(2, commentText.length()-ctx.END_COMMENT().getText().length()).trim();
        } else {
            commentText = commentText.substring(2).trim();
        }
        elementList.add(new TierComment(commentText));
    }

    @Override
    public void exitInternal_media(TierDataParser.Internal_mediaContext ctx) {
        final String startText = ctx.time_in_minutes_seconds(0).getText();
        final String endText = ctx.time_in_minutes_seconds(1).getText();

        try {
            final float startTime = MediaTimeFormatter.parseTimeToSeconds(startText);
            final float endTime = MediaTimeFormatter.parseTimeToSeconds(endText);

            final InternalMedia internalMedia = new InternalMedia(startTime, endTime);
            elementList.add(new TierInternalMedia(internalMedia));
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe);
        }

    }

    @Override
    public void exitLink(TierDataParser.LinkContext ctx) {
        final String label = ctx.label() != null ? ctx.label().getText() : null;
        int startIdx = TierLink.LINK_PREFIX.length() + (label != null ? label.length() + 1 : 0);
        int endIdx = ctx.getText().length() - TierLink.LINK_PREFIX.length();
        final String href = ctx.getText().substring(startIdx, endIdx);
        elementList.add(new TierLink(href, label));
    }

    public TierData toTierData() {
        return new TierData(elementList);
    }

}
