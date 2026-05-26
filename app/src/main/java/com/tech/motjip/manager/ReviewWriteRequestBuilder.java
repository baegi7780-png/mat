package com.tech.motjip.manager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ReviewWriteRequestBuilder {

    private static final MediaType TEXT_PLAIN =
            MediaType.parse(
                    "text/plain"
            );

    public static RequestData build(
            Long placeId,
            String placeName,
            Double latitude,
            Double longitude,
            Long memberId,
            int rating,
            boolean revisit,
            String content,
            String tagsString,
            MultipartBody.Part imagePart
    ) {

        return new RequestData(
                createTextBody(
                        String.valueOf(
                                placeId
                        )
                ),
                createTextBody(
                        placeName == null
                                ? ""
                                : placeName
                ),
                createTextBody(
                        latitude == null
                                ? ""
                                : String.valueOf(
                                latitude
                        )
                ),
                createTextBody(
                        longitude == null
                                ? ""
                                : String.valueOf(
                                longitude
                        )
                ),
                createTextBody(
                        String.valueOf(
                                memberId
                        )
                ),
                createTextBody(
                        String.valueOf(
                                rating
                        )
                ),
                createTextBody(
                        String.valueOf(
                                revisit
                        )
                ),
                createTextBody(
                        content
                ),
                createTextBody(
                        tagsString
                ),
                imagePart
        );
    }

    private static RequestBody createTextBody(
            String value
    ) {

        return RequestBody.create(
                TEXT_PLAIN,
                value == null
                        ? ""
                        : value
        );
    }

    public static class RequestData {

        private final RequestBody placeIdBody;
        private final RequestBody placeNameBody;
        private final RequestBody latitudeBody;
        private final RequestBody longitudeBody;
        private final RequestBody memberIdBody;
        private final RequestBody ratingBody;
        private final RequestBody revisitBody;
        private final RequestBody contentBody;
        private final RequestBody tagsBody;
        private final MultipartBody.Part imagePart;

        public RequestData(
                RequestBody placeIdBody,
                RequestBody placeNameBody,
                RequestBody latitudeBody,
                RequestBody longitudeBody,
                RequestBody memberIdBody,
                RequestBody ratingBody,
                RequestBody revisitBody,
                RequestBody contentBody,
                RequestBody tagsBody,
                MultipartBody.Part imagePart
        ) {

            this.placeIdBody =
                    placeIdBody;

            this.placeNameBody =
                    placeNameBody;

            this.latitudeBody =
                    latitudeBody;

            this.longitudeBody =
                    longitudeBody;

            this.memberIdBody =
                    memberIdBody;

            this.ratingBody =
                    ratingBody;

            this.revisitBody =
                    revisitBody;

            this.contentBody =
                    contentBody;

            this.tagsBody =
                    tagsBody;

            this.imagePart =
                    imagePart;
        }

        public RequestBody getPlaceIdBody() {
            return placeIdBody;
        }

        public RequestBody getPlaceNameBody() {
            return placeNameBody;
        }

        public RequestBody getLatitudeBody() {
            return latitudeBody;
        }

        public RequestBody getLongitudeBody() {
            return longitudeBody;
        }

        public RequestBody getMemberIdBody() {
            return memberIdBody;
        }

        public RequestBody getRatingBody() {
            return ratingBody;
        }

        public RequestBody getRevisitBody() {
            return revisitBody;
        }

        public RequestBody getContentBody() {
            return contentBody;
        }

        public RequestBody getTagsBody() {
            return tagsBody;
        }

        public MultipartBody.Part getImagePart() {
            return imagePart;
        }
    }
}