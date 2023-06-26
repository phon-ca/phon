package ca.phon.session.usertier;

import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.MediaTimeFormat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class UserTierParserListener extends UserTierBaseListener {

    final List<UserTierElement> elementList = new ArrayList<>();

    @Override
    public void exitWord(UserTierParser.WordContext ctx) {
        elementList.add(new TierString(ctx.getText()));
    }

    @Override
    public void exitComment(UserTierParser.CommentContext ctx) {
        final String commentText = ctx.getText().substring(2, ctx.getText().length()-1).trim();
        elementList.add(new UserTierComment(commentText));
    }

    @Override
    public void exitInternal_media(UserTierParser.Internal_mediaContext ctx) {
        final String startText = ctx.time_in_minutes_seconds(0).getText();
        final String endText = ctx.time_in_minutes_seconds(1).getText();

        final MediaTimeFormat timeFormat = new MediaTimeFormat();
        try {
            final float startTime = (Float) timeFormat.parseObject(startText);
            final float endTime = (Float) timeFormat.parseObject(endText);

            final InternalMedia internalMedia = new InternalMedia(startTime, endTime);
            elementList.add(new UserTierInternalMedia(internalMedia));
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe);
        }

    }

    public UserTierData toTierData() {
        return new UserTierData(elementList);
    }

}
