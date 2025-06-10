# 数据脱敏规则配置示例

本文档展示如何为不同的SQL查询配置不同的数据脱敏规则。

## 1. 全局脱敏规则

当所有SQL查询都使用相同的脱敏规则时，可以使用全局脱敏规则：

```json
{
  "sqlList": [
    "SELECT id, username, phone, email FROM users LIMIT 10",
    "SELECT id, customer_name, mobile, email_address FROM customers LIMIT 10"
  ],
  "sheetNames": ["用户数据", "客户数据"],
  "maskingRules": [
    {
      "fieldName": "phone",
      "maskingType": "PHONE",
      "enabled": true
    },
    {
      "fieldName": "mobile",
      "maskingType": "PHONE",
      "enabled": true
    },
    {
      "fieldName": "email",
      "maskingType": "EMAIL",
      "enabled": true
    },
    {
      "fieldName": "email_address",
      "maskingType": "EMAIL",
      "enabled": true
    }
  ]
}
```

## 2. 每个SQL独立脱敏规则

当不同的SQL查询需要不同的脱敏规则时，使用 `sqlMaskingRules` 字段：

```json
{
  "sqlList": [
    "SELECT id, username, phone, email FROM users LIMIT 10",
    "SELECT id, product_name, price, supplier_contact FROM products LIMIT 10",
    "SELECT order_id, customer_id_card, amount FROM orders LIMIT 10"
  ],
  "sheetNames": ["用户数据", "产品数据", "订单数据"],
  "sqlMaskingRules": [
    [
      // 第1个SQL的脱敏规则：脱敏手机号和邮箱
      {
        "fieldName": "phone",
        "maskingType": "PHONE",
        "enabled": true
      },
      {
        "fieldName": "email",
        "maskingType": "EMAIL",
        "enabled": true
      }
    ],
    [
      // 第2个SQL的脱敏规则：脱敏供应商联系方式
      {
        "fieldName": "supplier_contact",
        "maskingType": "PHONE",
        "enabled": true
      }
    ],
    [
      // 第3个SQL的脱敏规则：脱敏身份证号
      {
        "fieldName": "customer_id_card",
        "maskingType": "ID_CARD",
        "enabled": true
      }
    ]
  ]
}
```

## 3. 混合使用：全局规则 + 特定规则

可以同时使用全局脱敏规则和特定SQL脱敏规则。特定规则的优先级更高：

```json
{
  "sqlList": [
    "SELECT id, username, phone, email FROM users LIMIT 10",
    "SELECT id, customer_name, mobile, id_card FROM customers LIMIT 10",
    "SELECT id, product_name, price FROM products LIMIT 10"
  ],
  "sheetNames": ["用户数据", "客户数据", "产品数据"],
  "maskingRules": [
    // 全局规则：所有phone字段都脱敏
    {
      "fieldName": "phone",
      "maskingType": "PHONE",
      "enabled": true
    },
    {
      "fieldName": "mobile",
      "maskingType": "PHONE",
      "enabled": true
    }
  ],
  "sqlMaskingRules": [
    null,  // 第1个SQL使用全局规则
    [
      // 第2个SQL的特定规则：除了手机号，还要脱敏身份证
      {
        "fieldName": "mobile",
        "maskingType": "PHONE",
        "enabled": true
      },
      {
        "fieldName": "id_card",
        "maskingType": "ID_CARD",
        "enabled": true
      }
    ],
    []     // 第3个SQL不使用任何脱敏规则
  ]
}
```

## 4. 自定义脱敏规则示例

```json
{
  "sqlList": [
    "SELECT id, bank_account, credit_card FROM financial_data LIMIT 10"
  ],
  "sheetNames": ["金融数据"],
  "sqlMaskingRules": [
    [
      {
        "fieldName": "bank_account",
        "maskingType": "BANK_CARD",
        "enabled": true
      },
      {
        "fieldName": "credit_card",
        "maskingType": "CUSTOM",
        "customRegex": "(\\d{4})\\d{8}(\\d{4})",
        "customReplacement": "$1********$2",
        "enabled": true
      }
    ]
  ]
}
```

## 5. 规则优先级说明

1. **最高优先级**: `sqlMaskingRules` 中对应索引的规则
2. **次优先级**: `maskingRules` 全局规则
3. **最低优先级**: 无脱敏处理

### 规则匹配逻辑：

- 如果 `sqlMaskingRules[i]` 存在且不为空，使用该规则
- 否则，如果 `maskingRules` 存在且不为空，使用全局规则
- 否则，不进行脱敏处理

## 6. 实际使用示例

### 场景：用户数据导出

```bash
curl -X POST http://localhost:8080/api/sql-export/export \
  -H "Content-Type: application/json" \
  -d '{
    "sqlList": [
      "SELECT id, username, phone, email FROM users WHERE department = '销售部' LIMIT 100",
      "SELECT id, username, phone, email, id_card FROM users WHERE department = '人事部' LIMIT 50"
    ],
    "sheetNames": ["销售部用户", "人事部用户"],
    "sqlMaskingRules": [
      [
        {
          "fieldName": "phone",
          "maskingType": "PHONE",
          "enabled": true
        },
        {
          "fieldName": "email",
          "maskingType": "EMAIL",
          "enabled": true
        }
      ],
      [
        {
          "fieldName": "phone",
          "maskingType": "PHONE",
          "enabled": true
        },
        {
          "fieldName": "email",
          "maskingType": "EMAIL",
          "enabled": true
        },
        {
          "fieldName": "id_card",
          "maskingType": "ID_CARD",
          "enabled": true
        }
      ]
    ]
  }'
```

### 场景：财务数据导出（高安全级别）

```bash
curl -X POST http://localhost:8080/api/sql-export/export \
  -H "Content-Type: application/json" \
  -d '{
    "sqlList": [
      "SELECT customer_name, bank_account, amount FROM transactions LIMIT 100",
      "SELECT employee_name, salary, bank_info FROM payroll LIMIT 50"
    ],
    "sheetNames": ["交易记录", "工资单"],
    "sqlMaskingRules": [
      [
        {
          "fieldName": "customer_name",
          "maskingType": "NAME",
          "enabled": true
        },
        {
          "fieldName": "bank_account",
          "maskingType": "BANK_CARD",
          "enabled": true
        }
      ],
      [
        {
          "fieldName": "employee_name",
          "maskingType": "NAME",
          "enabled": true
        },
        {
          "fieldName": "salary",
          "maskingType": "CUSTOM",
          "customRegex": "(\\d+)",
          "customReplacement": "****",
          "enabled": true
        },
        {
          "fieldName": "bank_info",
          "maskingType": "BANK_CARD",
          "enabled": true
        }
      ]
    ]
  }'
```

## 7. 注意事项

1. **索引对应**: `sqlMaskingRules` 的索引必须与 `sqlList` 的索引对应
2. **null值处理**: 如果某个索引为 `null` 或空数组，则使用全局规则
3. **字段名匹配**: 脱敏规则中的 `fieldName` 必须与SQL查询结果中的列名完全匹配
4. **性能考虑**: 脱敏规则会影响导出性能，建议合理配置
5. **安全性**: 确保脱敏规则覆盖所有敏感字段

## 8. 错误处理

如果配置错误，系统会返回相应的错误信息：

- `sqlMaskingRules` 长度超过 `sqlList` 长度：会忽略多余的规则
- 无效的脱敏类型：会跳过该规则并记录警告
- 自定义正则表达式错误：会跳过该规则并记录错误