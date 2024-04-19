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
        if(ctx.END_BRACKET() != null) {
            commentText = commentText.substring(2, commentText.length()-ctx.END_BRACKET().getText().length()).trim();
        } else {
            commentText = commentText.substring(2).trim();
        }
        elementList.add(new TierComment(commentText));
    }


    @Override
    public void exitTime_in_minutes_seconds(TierDataParser.Time_in_minutes_secondsContext ctx) {
        if(ctx.exception != null) {
            if(ctx.getStart().getText().isBlank()) {
                throw new TierDataParserException(TierDataParserException.Type.MissingMediaBullet, "Missing media bullet", ctx.getStop().getCharPositionInLine());
            }
            throw new TierDataParserException(TierDataParserException.Type.InvalidTimeString, "Invalid time string " + ctx.getText(), ctx.getStart().getCharPositionInLine());
        }
        super.exitTime_in_minutes_seconds(ctx);
    }

    @Override
    public void exitInternal_media(TierDataParser.Internal_mediaContext ctx) {
        final String startText = ctx.time_in_minutes_seconds(0).getText();
        final String endText =
                ctx.time_in_minutes_seconds().size() > 1 ? ctx.time_in_minutes_seconds(1).getText() : startText;
        if(startText.isBlank() && endText.isBlank()
            && ctx.getStart() == ctx.getStop()) {
            // unique case where we are missing a start bullet
            throw new TierDataParserException(TierDataParserException.Type.MissingMediaBullet, "Missing media bullet", ctx.getStart().getCharPositionInLine());
        }

        float startTime = 0.0f;
        try {
            startTime = MediaTimeFormatter.parseTimeToSeconds(startText);
        } catch (ParseException pe) {
            throw new TierDataParserException(TierDataParserException.Type.InvalidTimeString, startText, ctx.time_in_minutes_seconds(0).getStart().getCharPositionInLine() + pe.getErrorOffset());
        }
        float endTime = 0.0f;
        try {
            endTime = MediaTimeFormatter.parseTimeToSeconds(endText);
            final InternalMedia internalMedia = new InternalMedia(startTime, endTime);
            elementList.add(new TierInternalMedia(internalMedia));
        } catch (ParseException pe) {
            throw new TierDataParserException(TierDataParserException.Type.InvalidTimeString, endText, ctx.time_in_minutes_seconds(1).getStart().getCharPositionInLine() + pe.getErrorOffset());
        }
    }

    @Override
    public void exitLink(TierDataParser.LinkContext ctx) {
        final String label = ctx.label() != null ? ctx.label().getText() : null;
        final String href = ctx.href() != null ? ctx.href().getText() : ".";
        elementList.add(new TierLink(href, label));
    }

    public TierData toTierData() {
        return new TierData(elementList);
    }

}
