package com.walmartlabs.concord.db;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.postgresql.util.PSQLException;

import java.sql.Timestamp;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;

public final class PgUtils {

    public static Condition contains(Field<String[]> left, String[] right) {
        return DSL.condition("{0} @> {1}::text[]", left, DSL.val(right, left.getDataType()));
    }

    public static Field<?> interval(String s) {
        return field("interval '" + s + "'");
    }

    public static Field<String> toChar(Field<Timestamp> date, String format) {
        return field("to_char({0}, {1})", String.class, date, inline(format));
    }

    public static Field<String> jsonbText(Field<JSONB> field, String name) {
        return field("{0}::jsonb->>{1}", Object.class, field, inline(name)).cast(String.class);
    }

    public static Field<String> jsonbTextByPath(Field<JSONB> field, List<String> path) {
        return field("{0}::jsonb #>> {1}", Object.class, field, inline(toPath(path))).cast(String.class);
    }

    public static Condition jsonbEq(Field<JSONB> field, String key, String value) {
        return DSL.condition("{0} @> jsonb_build_object({1}, {2})", field, DSL.val(key), DSL.value(value));
    }

    public static Condition jsonbContains(Field<JSONB> field, JSONB value) {
        return DSL.condition("{0} @> {1}", field, DSL.value(value));
    }

    public static Field<Integer> upperRange(Field<Object> field) {
        return DSL.field("upper({0})", Integer.class, field);
    }

    public static Field<Long> length(Field<byte[]> field) {
        return DSL.field("length({0})", Long.class, field);
    }

    public static boolean isUniqueViolationError(DataAccessException e) {
        Throwable cause = e.getCause();
        // see https://www.postgresql.org/docs/10/errcodes-appendix.html
        return cause instanceof PSQLException && ((PSQLException) e.getCause()).getSQLState().equals("23505");
    }

    private static String toPath(List<String> path) {
        return "{" + String.join(",", path) + "}";
    }

    private PgUtils() {
    }
}
