package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import com.hashedin.redmask.service.QueryBuilderUtil;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntegerRangeMasking extends ColumnRule {
  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskIntegerRange(config));
  }

  @Override
  public String getSubQuery(MaskConfiguration config, String tableName) throws IOException, TemplateException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getName());
    return QueryBuilderUtil.processQueryTemplate(config, MaskingConstants.MASK_INTEGER_RANGE_FUNC,paramsList);
  }
}
