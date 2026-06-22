package com.mycpt.backend.support;

import java.lang.reflect.Field;

public class EntityTestSupport {

    /**
     * 엔티티의 id 필드에 값을 주입한다.
     * 상속 계층을 거슬러 올라가며 id 필드를 탐색하므로
     * CTI 자식 엔티티(DiscTest 등)에도 사용 가능하다.
     */
    public static <T> T setId(T target, Long id) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
                return target;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("id 필드를 찾을 수 없음: " + target.getClass().getName());
    }

    /**
     * 리플렉션으로 엔티티의 임의 필드를 직접 세팅한다.
     * private 필드 + 정적 팩토리로만 생성 가능한 엔티티에서,
     * 정상적인 비즈니스 메서드 조합만으로는 만들기 번거로운 테스트 픽스처 상태를 구성할 때 사용한다.
     * <p>
     * 주의: 프로덕션 로직 우회이므로, 가능하면 엔티티의 정적 팩토리/비즈니스 메서드 조합을 우선 고려하고
     * 그게 비합리적으로 번거로울 때만 사용한다.
     */
    public static void setField(Object entity, String fieldName, Object value) {
        try {
            Field field = findField(entity.getClass(), fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 세팅 실패: " + entity.getClass().getSimpleName() + "." + fieldName, e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass == null) throw e;
            return findField(superclass, fieldName);
        }
    }
}
