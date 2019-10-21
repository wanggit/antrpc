package antrpc.monitor.web.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineChartVO<T extends Number> implements Serializable {

    private static final long serialVersionUID = -8010743822697964750L;
    private String title;
    private List<String> legends;
    private Map<String, Boolean> selectedLegends = new HashMap<>();
    private List<String> xAxies;
    private List<SeriesData<T>> series;

    public Map<String, Boolean> getSelectedLegends() {
        return selectedLegends;
    }

    public void setSelectedLegends(Map<String, Boolean> selectedLegends) {
        this.selectedLegends = selectedLegends;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLegends() {
        return legends;
    }

    public void setLegends(List<String> legends) {
        this.legends = legends;
    }

    public List<String> getxAxies() {
        return xAxies;
    }

    public void setxAxies(List<String> xAxies) {
        this.xAxies = xAxies;
    }

    public List<SeriesData<T>> getSeries() {
        return series;
    }

    public void setSeries(List<SeriesData<T>> series) {
        this.series = series;
    }

    public static class SeriesData<X extends Number> {
        private String name;
        private String type;
        private List<X> data;

        public SeriesData(String name, String type, List<X> data) {
            this.name = name;
            this.type = type;
            this.data = data;
        }

        public SeriesData() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<X> getData() {
            return data;
        }

        public void setData(List<X> data) {
            this.data = data;
        }
    }
}
