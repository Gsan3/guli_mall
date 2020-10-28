package com.tjj.gulimall.common.vail;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/9/7
 * Description:
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    //创建一个Set装载初始化的ListValue值
    Set<Integer> set =new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals){
            set.add(val);
        }

    }

    /**
     *
     * @param value:需要校验的值
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(value);
    }
}