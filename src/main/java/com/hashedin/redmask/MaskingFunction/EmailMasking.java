package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmailMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(EmailMasking.class);

  private static final String MASK_TYPE_SHOW_DOMAIN = "'domain'";
  private static final String MASK_TYPE_SHOW_FIRST_DOMAIN = "'firstndomain'";
  private static final String MASK_TYPE_SHOW_FIRST_CHAR = "'firstN'";
  private static final String MASK_TYPE_SHOW_SPECIAL_CHAR = "'nonspecialcharacter'";

  private static final String PARAM_SHOW_FIRST_CHARACTERS = "show_first";
  private static final String PARAM_SHOW_FIRST_CHARACTERS_DEFAULT = "0";


  public EmailMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public EmailMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskString(config));
      funcSet.add(MaskingQueryUtil.maskEmail(config));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws RedmaskConfigException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_EMAIL_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  private boolean validateAndAddParameters(List<String> parameters)
      throws RedmaskConfigException {
    for (String key : this.getMaskParams().keySet()) {
      if (!key.equals(PARAM_SHOW_FIRST_CHARACTERS)) {
        throw new RedmaskConfigException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }
    switch (this.getMaskType()) {
      case EMAIL_SHOW_DOMAIN:
        parameters.add(MASK_TYPE_SHOW_DOMAIN);
        break;

      case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        parameters.add(MASK_TYPE_SHOW_FIRST_DOMAIN);
        break;

      case EMAIL_SHOW_FIRST_CHARACTERS:
        parameters.add(MASK_TYPE_SHOW_FIRST_CHAR);

        if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
          parameters.add(PARAM_SHOW_FIRST_CHARACTERS_DEFAULT);
        }
        int showCharacters = Integer.parseInt(this.getMaskParams()
            .getOrDefault(PARAM_SHOW_FIRST_CHARACTERS, PARAM_SHOW_FIRST_CHARACTERS_DEFAULT));
        if (showCharacters > 0) {
          parameters.add(String.valueOf(showCharacters));
        } else {
          throw new RedmaskConfigException(
              String.format("\'%s\' value should be greater than 0",
                  PARAM_SHOW_FIRST_CHARACTERS));
        }
        break;
      case EMAIL_MASK_ALPHANUMERIC:
        parameters.add(MASK_TYPE_SHOW_SPECIAL_CHAR);
        break;
      default:
        return false;
    }
    return true;
  }
}
