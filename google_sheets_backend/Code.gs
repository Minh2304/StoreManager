/**
 * ============================================================================
 * AFADA ĐỒ UỐNG - HỆ THỐNG ĐĂNG NHẬP, OTP GMAIL & ĐA CƠ SỞ DỮ LIỆU GOOGLE SHEETS
 * ============================================================================
 * Các tính năng tự động của Script này:
 * 1. Đăng ký tài khoản mới + Gửi mã OTP 6 số thật về Gmail (MailApp.sendEmail).
 * 2. Đăng nhập bằng Gmail + Mật khẩu trên nhiều thiết bị KHÔNG CẦN xác thực lại.
 * 3. Quên mật khẩu: Gửi mã OTP 6 số về Gmail và đặt lại mật khẩu mới.
 * 4. Quản lý danh sách Cửa hàng (Databases) theo từng Gmail:
 *    - Nhấn nút (+) đặt tên cửa hàng -> Tự động tạo 1 file Google Spreadsheet mới
 *      trên Google Drive (SpreadsheetApp.create) và chia sẻ quyền chỉnh sửa thẳng
 *      vào Gmail của người dùng!
 *    - Mỗi cửa hàng có đầy đủ 6 bảng: Products, ProductUnits, Customers, Orders,
 *      OrderItems, DebtTransactions.
 * ============================================================================
 */

const SYSTEM_SHEETS = {
  ACCOUNTS: 'Sys_Accounts',
  OTP_CODES: 'Sys_OtpCodes',
  DATABASES: 'Sys_Databases'
};

const STORE_SHEET_HEADERS = {
  Products: [
    'productId', 'name', 'category', 'baseUnit',
    'baseCostPrice', 'baseSellPrice', 'stockBaseQty',
    'minStockAlert', 'updatedAt'
  ],
  ProductUnits: [
    'unitId', 'productId', 'unitName', 'conversionRate',
    'sellPrice', 'costPrice', 'barcode', 'isDefault'
  ],
  Customers: [
    'customerId', 'name', 'phone', 'address',
    'totalDebt', 'emptyCrateNote', 'updatedAt'
  ],
  Orders: [
    'orderId', 'orderDate', 'customerId', 'customerName',
    'totalAmount', 'discount', 'finalAmount', 'paidAmount',
    'debtAmount', 'paymentMethod', 'note'
  ],
  OrderItems: [
    'itemId', 'orderId', 'productId', 'productName',
    'unitId', 'unitName', 'conversionRate', 'quantity',
    'unitPrice', 'costPrice', 'lineTotal'
  ],
  DebtTransactions: [
    'transId', 'date', 'customerId', 'customerName',
    'amount', 'type', 'note'
  ]
};

/**
 * Chạy hàm này 1 lần đầu tiên để khởi tạo các bảng Hệ thống và Cửa hàng mặc định
 */
function setupSheets() {
  const masterSs = SpreadsheetApp.getActiveSpreadsheet();
  ensureSystemSheets(masterSs);
  ensureStoreSheets(masterSs);
}

function ensureSystemSheets(ss) {
  initSheetWithHeaders(ss, SYSTEM_SHEETS.ACCOUNTS, ['email', 'password', 'createdAt']);
  initSheetWithHeaders(ss, SYSTEM_SHEETS.OTP_CODES, ['email', 'otp', 'expiresAt']);
  initSheetWithHeaders(ss, SYSTEM_SHEETS.DATABASES, ['dbId', 'ownerEmail', 'dbName', 'spreadsheetId', 'spreadsheetUrl', 'createdAt']);
}

function ensureStoreSheets(ss) {
  Object.keys(STORE_SHEET_HEADERS).forEach(sheetName => {
    initSheetWithHeaders(ss, sheetName, STORE_SHEET_HEADERS[sheetName]);
  });
}

function initSheetWithHeaders(ss, sheetName, headers) {
  let sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
  }
  if (sheet.getLastRow() === 0) {
    sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    const headerRange = sheet.getRange(1, 1, 1, headers.length);
    headerRange.setBackground('#0D47A1');
    headerRange.setFontColor('#FFFFFF');
    headerRange.setFontWeight('bold');
    sheet.setFrozenRows(1);
  }
}

/**
 * Xử lý GET Request:
 * - ?action=ping
 * - ?action=listDbs&email=...
 * - ?action=pullAll&dbId=...
 */
function doGet(e) {
  const masterSs = SpreadsheetApp.getActiveSpreadsheet();
  ensureSystemSheets(masterSs);

  const action = (e && e.parameter && e.parameter.action) ? e.parameter.action : 'pullAll';

  if (action === 'ping') {
    return jsonResponse({
      status: 'success',
      message: 'Kết nối Google Sheets thành công!',
      spreadsheetName: masterSs.getName()
    });
  }

  if (action === 'listDbs') {
    const email = ((e && e.parameter && e.parameter.email) || '').trim().toLowerCase();
    const allDbs = readSheetObjects(masterSs, SYSTEM_SHEETS.DATABASES);
    const userDbs = email ? allDbs.filter(d => String(d.ownerEmail).toLowerCase() === email) : allDbs;
    return jsonResponse({
      status: 'success',
      databases: userDbs
    });
  }

  const dbId = (e && e.parameter && e.parameter.dbId) ? e.parameter.dbId : '';
  const targetSs = getTargetSpreadsheet(masterSs, dbId);
  ensureStoreSheets(targetSs);

  const payload = {
    status: 'success',
    spreadsheetName: targetSs.getName(),
    products: readSheetObjects(targetSs, 'Products'),
    productUnits: readSheetObjects(targetSs, 'ProductUnits'),
    customers: readSheetObjects(targetSs, 'Customers'),
    orders: readSheetObjects(targetSs, 'Orders'),
    orderItems: readSheetObjects(targetSs, 'OrderItems'),
    debtTransactions: readSheetObjects(targetSs, 'DebtTransactions')
  };

  return jsonResponse(payload);
}

/**
 * Xử lý POST Request cho Đăng nhập, Đăng ký OTP, Quên mật khẩu, Tạo Database & Đồng bộ
 */
function doPost(e) {
  try {
    const masterSs = SpreadsheetApp.getActiveSpreadsheet();
    ensureSystemSheets(masterSs);

    const body = JSON.parse(e.postData.contents);
    const action = body.action || 'pushAll';

    // 1. GỬI MÃ OTP XÁC THỰC VỀ GMAIL (Dùng cho Đăng ký & Quên mật khẩu)
    if (action === 'sendOtp') {
      const email = String(body.email || '').trim().toLowerCase();
      if (!email) return jsonResponse({ status: 'error', message: 'Vui lòng nhập Gmail!' });

      const otp = Math.floor(100000 + Math.random() * 900000).toString();
      const expiresAt = new Date().getTime() + 10 * 60 * 1000; // Hiệu lực 10 phút

      const otpList = readSheetObjects(masterSs, SYSTEM_SHEETS.OTP_CODES)
        .filter(item => String(item.email).toLowerCase() !== email);
      otpList.push({ email: email, otp: otp, expiresAt: expiresAt });
      overwriteSheetObjects(masterSs, SYSTEM_SHEETS.OTP_CODES, ['email', 'otp', 'expiresAt'], otpList);

      try {
        MailApp.sendEmail({
          to: email,
          subject: '[AFaDa Đồ Uống] Mã xác thực: ' + otp,
          body: 'Xin chào,\n\nMã xác thực (OTP) của bạn là: ' + otp + '\n\nMã có hiệu lực trong 10 phút.'
        });
      } catch (mailErr) {
        // Trường hợp chạy thử chưa cấp quyền MailApp thì trả kèm mã trong message
        return jsonResponse({
          status: 'success',
          message: 'Mã xác thực của bạn là: ' + otp,
          debugOtp: otp
        });
      }

      return jsonResponse({
        status: 'success',
        message: 'Đã gửi mã 6 số vào Gmail ' + email
      });
    }

    // 2. ĐĂNG KÝ TÀI KHOẢN (Cần kiểm tra mã OTP)
    if (action === 'register') {
      const email = String(body.email || '').trim().toLowerCase();
      const password = String(body.password || '').trim();
      const otp = String(body.otp || '').trim();

      if (!verifyOtpCode(masterSs, email, otp)) {
        return jsonResponse({ status: 'error', message: 'Mã xác thực 6 số không đúng!' });
      }

      const accounts = readSheetObjects(masterSs, SYSTEM_SHEETS.ACCOUNTS);
      if (accounts.some(acc => String(acc.email).toLowerCase() === email)) {
        return jsonResponse({ status: 'error', message: 'Gmail này đã được đăng ký rồi!' });
      }

      accounts.push({
        email: email,
        password: password,
        createdAt: new Date().getTime()
      });
      overwriteSheetObjects(masterSs, SYSTEM_SHEETS.ACCOUNTS, ['email', 'password', 'createdAt'], accounts);

      return jsonResponse({
        status: 'success',
        message: 'Đăng ký thành công!',
        databases: []
      });
    }

    // 3. QUÊN MẬT KHẨU (Cần kiểm tra mã OTP)
    if (action === 'resetPassword') {
      const email = String(body.email || '').trim().toLowerCase();
      const newPassword = String(body.newPassword || '').trim();
      const otp = String(body.otp || '').trim();

      if (!verifyOtpCode(masterSs, email, otp)) {
        return jsonResponse({ status: 'error', message: 'Mã xác thực 6 số không đúng!' });
      }

      const accounts = readSheetObjects(masterSs, SYSTEM_SHEETS.ACCOUNTS);
      let found = false;
      accounts.forEach(acc => {
        if (String(acc.email).toLowerCase() === email) {
          acc.password = newPassword;
          found = true;
        }
      });
      if (!found) {
        accounts.push({ email: email, password: newPassword, createdAt: new Date().getTime() });
      }
      overwriteSheetObjects(masterSs, SYSTEM_SHEETS.ACCOUNTS, ['email', 'password', 'createdAt'], accounts);

      return jsonResponse({
        status: 'success',
        message: 'Đã đổi mật khẩu mới thành công!'
      });
    }

    // 4. ĐĂNG NHẬP NHANH TRÊN NHIỀU THIẾT BỊ (KHÔNG CẦN OTP)
    if (action === 'login') {
      const email = String(body.email || '').trim().toLowerCase();
      const password = String(body.password || '').trim();

      const accounts = readSheetObjects(masterSs, SYSTEM_SHEETS.ACCOUNTS);
      const matched = accounts.find(acc => String(acc.email).toLowerCase() === email && String(acc.password) === password);
      if (!matched) {
        return jsonResponse({ status: 'error', message: 'Sai Gmail hoặc mật khẩu!' });
      }

      const allDbs = readSheetObjects(masterSs, SYSTEM_SHEETS.DATABASES);
      const userDbs = allDbs.filter(d => String(d.ownerEmail).toLowerCase() === email);

      return jsonResponse({
        status: 'success',
        message: 'Đăng nhập thành công!',
        databases: userDbs
      });
    }

    // 5. TẠO DATABASE MỚI TRÊN GOOGLE DRIVE / GMAIL CỦA NGƯỜI DÙNG
    if (action === 'createDb') {
      const email = String(body.email || '').trim().toLowerCase();
      const dbName = String(body.dbName || 'Cửa Hàng Mới').trim();
      const dbId = body.dbId || ('DB_' + new Date().getTime());

      // Tự động tạo 1 file Google Spreadsheet mới với đúng tên của Cửa hàng
      const newSs = SpreadsheetApp.create(dbName);
      ensureStoreSheets(newSs);

      // Chia sẻ thẳng vào Gmail của người dùng để họ thấy trong Google Drive / Sheets
      try {
        if (email && email.includes('@')) {
          newSs.addEditor(email);
        }
      } catch (shareErr) {
        // Bỏ qua nếu email chính là chủ sở hữu hiện tại
      }

      const allDbs = readSheetObjects(masterSs, SYSTEM_SHEETS.DATABASES);
      const newRecord = {
        dbId: dbId,
        ownerEmail: email,
        dbName: dbName,
        spreadsheetId: newSs.getId(),
        spreadsheetUrl: newSs.getUrl(),
        createdAt: new Date().getTime()
      };
      allDbs.push(newRecord);
      overwriteSheetObjects(
        masterSs,
        SYSTEM_SHEETS.DATABASES,
        ['dbId', 'ownerEmail', 'dbName', 'spreadsheetId', 'spreadsheetUrl', 'createdAt'],
        allDbs
      );

      return jsonResponse({
        status: 'success',
        message: 'Đã tự động tạo Google Sheet "' + dbName + '" trên Gmail!',
        database: newRecord
      });
    }

    // 5B. XÓA CỬA HÀNG (DATABASE) KHI NGƯỜI DÙNG XÁC NHẬN ĐÚNG MẬT KHẨU ĐĂNG NHẬP
    if (action === 'deleteDb') {
      const email = String(body.email || '').trim().toLowerCase();
      const password = String(body.password || '').trim();
      const dbId = String(body.dbId || '').trim();

      const accounts = readSheetObjects(masterSs, SYSTEM_SHEETS.ACCOUNTS);
      const matchedAcc = accounts.find(acc => String(acc.email).toLowerCase() === email);
      if (matchedAcc && String(matchedAcc.password) !== password) {
        return jsonResponse({ status: 'error', message: 'Mật khẩu đăng nhập không đúng!' });
      }

      const allDbs = readSheetObjects(masterSs, SYSTEM_SHEETS.DATABASES);
      const targetDb = allDbs.find(d => String(d.dbId) === dbId);

      // Chuyển file Google Sheet của cửa hàng đó vào Thùng rác (Trash) trên Google Drive
      if (targetDb && targetDb.spreadsheetId) {
        try {
          DriveApp.getFileById(String(targetDb.spreadsheetId)).setTrashed(true);
        } catch (trashErr) {
        }
      }

      const remainingDbs = allDbs.filter(d => String(d.dbId) !== dbId);
      overwriteSheetObjects(
        masterSs,
        SYSTEM_SHEETS.DATABASES,
        ['dbId', 'ownerEmail', 'dbName', 'spreadsheetId', 'spreadsheetUrl', 'createdAt'],
        remainingDbs
      );

      return jsonResponse({
        status: 'success',
        message: 'Đã xóa cửa hàng thành công!'
      });
    }

    // 6. ĐỒNG BỘ DỮ LIỆU BÁN HÀNG LÊN DATABASE ĐANG CHỌN (pushAll)
    const targetSs = getTargetSpreadsheet(masterSs, body.dbId || '');
    ensureStoreSheets(targetSs);

    if (body.products) {
      overwriteSheetObjects(targetSs, 'Products', STORE_SHEET_HEADERS.Products, body.products);
    }
    if (body.productUnits) {
      overwriteSheetObjects(targetSs, 'ProductUnits', STORE_SHEET_HEADERS.ProductUnits, body.productUnits);
    }
    if (body.customers) {
      overwriteSheetObjects(targetSs, 'Customers', STORE_SHEET_HEADERS.Customers, body.customers);
    }
    if (body.orders) {
      overwriteSheetObjects(targetSs, 'Orders', STORE_SHEET_HEADERS.Orders, body.orders);
    }
    if (body.orderItems) {
      overwriteSheetObjects(targetSs, 'OrderItems', STORE_SHEET_HEADERS.OrderItems, body.orderItems);
    }
    if (body.debtTransactions) {
      overwriteSheetObjects(targetSs, 'DebtTransactions', STORE_SHEET_HEADERS.DebtTransactions, body.debtTransactions);
    }

    return jsonResponse({
      status: 'success',
      message: 'Đã lưu lên Google Sheets thành công!',
      syncedAt: new Date().getTime()
    });
  } catch (err) {
    return jsonResponse({
      status: 'error',
      message: err.toString()
    });
  }
}

function getTargetSpreadsheet(masterSs, dbId) {
  if (!dbId) return masterSs;
  const allDbs = readSheetObjects(masterSs, SYSTEM_SHEETS.DATABASES);
  const matched = allDbs.find(d => String(d.dbId) === String(dbId));
  if (matched && matched.spreadsheetId) {
    try {
      return SpreadsheetApp.openById(String(matched.spreadsheetId));
    } catch (e) {
      return masterSs;
    }
  }
  return masterSs;
}

function verifyOtpCode(masterSs, email, otp) {
  if (!otp) return false;
  const otpList = readSheetObjects(masterSs, SYSTEM_SHEETS.OTP_CODES);
  const record = otpList.find(item => String(item.email).toLowerCase() === email && String(item.otp) === otp);
  if (!record) return false;
  return new Date().getTime() <= Number(record.expiresAt);
}

function readSheetObjects(ss, sheetName) {
  const sheet = ss.getSheetByName(sheetName);
  if (!sheet || sheet.getLastRow() <= 1) return [];
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  const result = [];

  for (let i = 1; i < data.length; i++) {
    const row = data[i];
    if (row[0] === '' || row[0] === null) continue;
    const obj = {};
    for (let j = 0; j < headers.length; j++) {
      obj[headers[j]] = row[j];
    }
    result.push(obj);
  }
  return result;
}

function overwriteSheetObjects(ss, sheetName, headers, items) {
  let sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
  }
  sheet.clearContents();
  sheet.getRange(1, 1, 1, headers.length).setValues([headers]);

  if (!items || items.length === 0) return;

  const rows = items.map(item => {
    return headers.map(h => (item[h] !== undefined && item[h] !== null) ? item[h] : '');
  });

  sheet.getRange(2, 1, rows.length, headers.length).setValues(rows);
}

function jsonResponse(obj) {
  return ContentService
    .createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
