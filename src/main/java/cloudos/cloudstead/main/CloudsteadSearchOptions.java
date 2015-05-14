package cloudos.cloudstead.main;

import cloudos.cloudstead.model.support.CloudsteadEntityType;
import cloudos.cloudstead.model.support.CloudsteadSearchRequest;
import lombok.Getter;
import org.cobbzilla.wizard.model.ResultPage;
import org.kohsuke.args4j.Option;

public class CloudsteadSearchOptions extends CloudsteadMainOptions {

    @Getter CloudsteadSearchRequest searchRequest = new CloudsteadSearchRequest();

    public ResultPage getResultPage () { return searchRequest.getPage(); }

    public static final String USAGE_TYPE = "Type of thing to search for";
    public static final String OPT_TYPE = "-t";
    public static final String LONGOPT_TYPE = "--type";
    @Option(name=OPT_TYPE, aliases=LONGOPT_TYPE, usage=USAGE_TYPE, required=true)
    public void setType (CloudsteadEntityType type) { searchRequest.setType(type); }

    public String getType() { return searchRequest.getType().name(); }

    public static final String USAGE_PAGENUMBER = "Page number";
    public static final String OPT_PAGENUMBER = "-N";
    public static final String LONGOPT_PAGENUMBER = "--pagenum";
    @Option(name=OPT_PAGENUMBER, aliases=LONGOPT_PAGENUMBER, usage=USAGE_PAGENUMBER)
    public void setPageNumber (int num) { searchRequest.getPage().setPageNumber(num); }

    public static final String USAGE_PAGESIZE = "Page size";
    public static final String OPT_PAGESIZE = "-S";
    public static final String LONGOPT_PAGESIZE = "--pagesize";
    @Option(name=OPT_PAGESIZE, aliases=LONGOPT_PAGESIZE, usage=USAGE_PAGESIZE)
    public void setPageSize (int size) { searchRequest.getPage().setPageSize(size); }

    public static final String USAGE_QUERY = "Query";
    public static final String OPT_QUERY = "-q";
    public static final String LONGOPT_QUERY = "--query";
    @Option(name=OPT_QUERY, aliases=LONGOPT_QUERY, usage=USAGE_QUERY)
    public void setQuery (String filter) { searchRequest.getPage().setFilter(filter); }

    public static final String USAGE_SORTFIELD = "Sort field";
    public static final String OPT_SORTFIELD = "-f";
    public static final String LONGOPT_SORTFIELD = "--sortfield";
    @Option(name=OPT_SORTFIELD, aliases=LONGOPT_SORTFIELD, usage=USAGE_SORTFIELD)
    public void setSortField (String field) { searchRequest.getPage().setSortField(field); }

    public static final String USAGE_SORTORDER = "Sort order";
    public static final String OPT_SORTORDER = "-o";
    public static final String LONGOPT_SORTORDER = "--sortorder";
    @Option(name=OPT_SORTORDER, aliases=LONGOPT_SORTORDER, usage=USAGE_SORTORDER)
    public void setSortOrder (ResultPage.SortOrder order) { searchRequest.getPage().setSortOrder(order); }

}
