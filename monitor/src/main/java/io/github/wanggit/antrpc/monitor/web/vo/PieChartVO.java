package io.github.wanggit.antrpc.monitor.web.vo;

import java.io.Serializable;
import java.util.List;

public class PieChartVO<T extends Number> implements Serializable {
    private static final long serialVersionUID = -6797244944854094L;
    private String title;
    private List<SeriesData<T>> series;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SeriesData<T>> getSeries() {
        return series;
    }

    public void setSeries(List<SeriesData<T>> series) {
        this.series = series;
    }

    public static class SeriesData<X extends Number> {
        private X value;
        private String name;

        public X getValue() {
            return value;
        }

        public void setValue(X value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
