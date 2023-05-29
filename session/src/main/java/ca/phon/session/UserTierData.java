package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.MediaTimeFormat;
import ca.phon.orthography.TagMarkerType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class UserTierData extends ExtendableObject {

    private final List<UserTierElement> elements;

    public static UserTierData parseTierData(String text) throws ParseException {
        final String[] parts = text.split("\\p{Space}");
        final String commentPattern = "\\((.+)\\)";
        final String internalMediaPattern = InternalMedia.MEDIA_BULLET
                + "(" + MediaTimeFormat.PATTERN
                + "-"
                + MediaTimeFormat.PATTERN + ")"
                + InternalMedia.MEDIA_BULLET;

        final List<UserTierElement> elements = new ArrayList<>();
        for(String part:parts) {
            if(part.matches(commentPattern)) {
                final Pattern pattern = Pattern.compile(commentPattern);
                final Matcher matcher = pattern.matcher(part);
                if(matcher.matches()) {
                    final UserTierComment comment = new UserTierComment(matcher.group(1));
                    elements.add(comment);
                }
            } else if(part.matches(internalMediaPattern)) {
                final Pattern pattern = Pattern.compile(internalMediaPattern);
                final Matcher matcher = pattern.matcher(part);
                if(matcher.matches()) {
                    final String segmentText = matcher.group(1);
                    final String[] startEnd = segmentText.split("-");
                    final MediaTimeFormat mediaTimeFormat = new MediaTimeFormat();
                    try {
                        final float startTime = (Float) mediaTimeFormat.parseObject(startEnd[0]);
                        final float endTime = (float) mediaTimeFormat.parseObject(startEnd[1]);
                        elements.add(new UserTierInternalMedia(new InternalMedia(startTime, endTime)));
                    } catch (ParseException pe) {
                        throw new ParseException(pe.getMessage(), pe.getErrorOffset());
                    }
                }
            } else {
                if(part.length() == 1) {
                    final char firstChar = part.charAt(0);
                    final TagMarkerType tagMarkerType = TagMarkerType.fromChar(firstChar);
                    if(tagMarkerType != null) {
                        elements.add(new UserTierTagMarker(tagMarkerType));
                    } else {
                        elements.add(new TierString(part));
                    }
                } else {
                    final char lastChar = part.charAt(part.length()-1);
                    final TagMarkerType tagMarkerType = TagMarkerType.fromChar(lastChar);
                    if(tagMarkerType != null) {
                        elements.add(new TierString(part.substring(part.length()-1)));
                        elements.add(new UserTierTagMarker(tagMarkerType));
                    } else {
                        elements.add(new TierString(part));
                    }
                }
            }
        }
        return new UserTierData(elements);
    }

    public UserTierData(UserTierElement ... elements) {
        this(Arrays.asList(elements));
    }

    public UserTierData(List<UserTierElement> elements) {
        super();
        this.elements = Collections.unmodifiableList(elements);
    }

    public List<UserTierElement> getElements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }

    public UserTierElement elementAt(int index) {
        return elements.get(index);
    }

    @Override
    public String toString() {
        return getElements().stream().map(ele -> ele.toString()).collect(Collectors.joining(" "));
    }

}
