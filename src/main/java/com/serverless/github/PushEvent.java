package com.serverless.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A wrapper for https://developer.github.com/v3/activity/events/types/#pushevent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushEvent {

    private String ref;
    private Repository repository;
    private String compare;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getCompare() {
        return compare;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Repository {

        private Owner owner;
        private String name;
        private String url;

        public Owner getOwner() {
            return owner;
        }

        public void setOwner(Owner owner) {
            this.owner = owner;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Owner {

            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
