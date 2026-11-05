package com.slimcorp.muilive;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ShapeConfig config;

    // Views
    private WallpaperPreviewView preview;
    private TextView previewStatusPill;
    private TextView previewSeedText;
    private TabLayout tabLayout;

    // Tab Containers
    private View tabContentStyle;
    private View tabContentColors;
    private View tabContentMotion;
    private View tabContentShare;

    // Style Tab Views
    private LinearLayout layoutPresetsRow;
    private ChipGroup chipsShapes;
    private Slider sliderSize, sliderPetals, sliderSpacing;
    private TextView valueSize, valuePetals, valueSpacing;

    // Colors Tab Views
    private MaterialSwitch switchDynamic;
    private View cardManualColor;
    private LinearLayout layoutManualColors;
    private MaterialSwitch switchSecondary;
    private View layoutSecondaryControls;
    private Slider sliderExtraCount;
    private TextView textExtraCount;
    private MaterialButton btnColorSec1, btnColorSec2, btnColorSec3;
    private MaterialSwitch switchCustomBg;
    private MaterialButton btnBgColor;
    private MaterialButton btnPickPhoto, btnClearPhoto;
    private TextView textPhotoStatus;

    // Motion Tab Views
    private Slider sliderSpeed, sliderOpacity, sliderBlur;
    private TextView valueSpeed, valueOpacity, valueBlur;
    private MaterialSwitch switchPulse;

    private final androidx.activity.result.ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        config.photo = uri.toString();
                        saveAndRefresh(false);
                        Toast.makeText(this, "Фото фона установлено", Toast.LENGTH_SHORT).show();
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Не удалось сохранить доступ к фото", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    // QR scanner launcher
    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> qrScanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) return;
        try {
            ShapeConfig imported = SettingsCode.decode(result.getContents().trim(), config);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Применить импортированный стиль?")
                    .setMessage("Текущие параметры сетки и цвета будут обновлены.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Применить", (dialog, which) -> {
                        config = imported;
                        saveAndRefresh(true);
                    })
                    .show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "QR-код не содержит корректных настроек mUI Live", Toast.LENGTH_LONG).show();
        }
    });

    private final int[] availableColors = {
            0xFF6750A4, 0xFF3B82F6, 0xFF10B981, 0xFFF59E0B,
            0xFFF97316, 0xFFEF4444, 0xFFEC4899, 0xFF06B6D4,
            0xFF8B5CF6, 0xFF14B8A6, 0xFF6366F1, 0xFFE11D48
    };

    private final String[] shapeNames = {
            "Круг", "Сквиркл", "Цветок 8", "Цветок 5", "Шестиугольник", "Солнце", "Капля"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        config = ShapeConfig.load(this);

        setContentView(R.layout.activity_main);

        initViews();
        setupInsets();
        setupTabs();
        setupStyleTab();
        setupColorsTab();
        setupMotionTab();
        setupShareTab();
        setupHeaderAndBottomActions();

        updateUiFromConfig();
        handleTabIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleTabIntent(intent);
    }

    private void handleTabIntent(Intent intent) {
        if (intent != null && intent.hasExtra("tab") && tabLayout != null) {
            int tabIdx = intent.getIntExtra("tab", 0);
            if (tabIdx >= 0 && tabIdx < tabLayout.getTabCount()) {
                TabLayout.Tab tab = tabLayout.getTabAt(tabIdx);
                if (tab != null) {
                    tab.select();
                }
                tabContentStyle.setVisibility(tabIdx == 0 ? View.VISIBLE : View.GONE);
                tabContentColors.setVisibility(tabIdx == 1 ? View.VISIBLE : View.GONE);
                tabContentMotion.setVisibility(tabIdx == 2 ? View.VISIBLE : View.GONE);
                tabContentShare.setVisibility(tabIdx == 3 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void initViews() {
        preview = findViewById(R.id.wallpaperPreview);
        previewStatusPill = findViewById(R.id.previewStatusPill);
        previewSeedText = findViewById(R.id.previewSeedText);
        tabLayout = findViewById(R.id.tabLayout);

        tabContentStyle = findViewById(R.id.tabContentStyle);
        tabContentColors = findViewById(R.id.tabContentColors);
        tabContentMotion = findViewById(R.id.tabContentMotion);
        tabContentShare = findViewById(R.id.tabContentShare);

        // Style
        layoutPresetsRow = findViewById(R.id.layoutPresetsRow);
        chipsShapes = findViewById(R.id.chipsShapes);
        sliderSize = findViewById(R.id.sliderSize);
        sliderPetals = findViewById(R.id.sliderPetals);
        sliderSpacing = findViewById(R.id.sliderSpacing);
        valueSize = findViewById(R.id.valueSize);
        valuePetals = findViewById(R.id.valuePetals);
        valueSpacing = findViewById(R.id.valueSpacing);

        // Colors
        switchDynamic = findViewById(R.id.switchDynamic);
        cardManualColor = findViewById(R.id.cardManualColor);
        layoutManualColors = findViewById(R.id.layoutManualColors);
        switchSecondary = findViewById(R.id.switchSecondary);
        layoutSecondaryControls = findViewById(R.id.layoutSecondaryControls);
        sliderExtraCount = findViewById(R.id.sliderExtraCount);
        textExtraCount = findViewById(R.id.textExtraCount);
        btnColorSec1 = findViewById(R.id.btnColorSec1);
        btnColorSec2 = findViewById(R.id.btnColorSec2);
        btnColorSec3 = findViewById(R.id.btnColorSec3);
        switchCustomBg = findViewById(R.id.switchCustomBg);
        btnBgColor = findViewById(R.id.btnBgColor);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        btnClearPhoto = findViewById(R.id.btnClearPhoto);
        textPhotoStatus = findViewById(R.id.textPhotoStatus);

        // Motion
        sliderSpeed = findViewById(R.id.sliderSpeed);
        sliderOpacity = findViewById(R.id.sliderOpacity);
        sliderBlur = findViewById(R.id.sliderBlur);
        valueSpeed = findViewById(R.id.valueSpeed);
        valueOpacity = findViewById(R.id.valueOpacity);
        valueBlur = findViewById(R.id.valueBlur);
        switchPulse = findViewById(R.id.switchPulse);
    }

    private void setupInsets() {
        View root = findViewById(R.id.rootContainer);
        View headerBar = findViewById(R.id.headerBar);
        View bottomBar = findViewById(R.id.bottomBarContainer);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            headerBar.setPadding(dp(20), statusBars.top + dp(10), dp(20), dp(8));
            bottomBar.setPadding(dp(20), dp(10), dp(20), navBars.bottom + dp(14));
            return insets;
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                haptic(tabLayout);
                int position = tab.getPosition();
                tabContentStyle.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                tabContentColors.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                tabContentMotion.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
                tabContentShare.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupStyleTab() {
        buildPresetsList();
        buildShapesChips();

        sliderSize.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.sizeMultiplier = value;
                saveAndRefresh(false);
            }
        });

        sliderPetals.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.petalDepth = value;
                saveAndRefresh(false);
            }
        });

        sliderSpacing.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.spacing = value;
                saveAndRefresh(false);
            }
        });
    }

    private void buildPresetsList() {
        layoutPresetsRow.removeAllViews();
        String[] titles = {"Material", "Конфетти", "Мягкий", "Минимал", "Контраст"};
        String[] emojis = {"🟣", "🎊", "🌸", "⚡", "🌙"};

        for (int i = 0; i < titles.length; i++) {
            final int preset = i;
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setBackgroundTintList(ColorStateList.valueOf(0xFF1E202E));
            btn.setStrokeColor(ColorStateList.valueOf(0xFF2B2D40));
            btn.setTextColor(0xFFF4F3F8);
            btn.setText(emojis[i] + " " + titles[i]);
            btn.setAllCaps(false);
            btn.setTextSize(13);
            btn.setCornerRadius(dp(18));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(44));
            lp.setMarginEnd(dp(8));
            btn.setLayoutParams(lp);

            btn.setOnClickListener(v -> {
                haptic(btn);
                applyPreset(preset);
            });
            layoutPresetsRow.addView(btn);
        }
    }

    private void applyPreset(int preset) {
        config.shapeMask = new int[]{127, 127, 12, 3, 112}[preset];
        config.opacity = new float[]{1.0f, 0.85f, 0.65f, 1.0f, 0.95f}[preset];
        config.blur = new float[]{0f, 0f, 0.45f, 0f, 0f}[preset];
        config.spacing = new float[]{1.0f, 0.80f, 1.2f, 1.35f, 0.9f}[preset];
        config.useSecondary = (preset == 1 || preset == 4);
        if (preset == 1) {
            config.secondaryColor = 0xFFFF92BD;
            config.extra2 = 0xFF82B1FF;
            config.extra3 = 0xFFFFD180;
            config.extraCount = 3;
            config.pulse = true;
        } else if (preset == 4) {
            config.secondaryColor = 0xFFFFB45C;
            config.extraCount = 1;
            config.customBackground = true;
            config.backgroundColor = 0xFF0D111A;
            config.pulse = false;
        } else {
            config.customBackground = false;
            config.pulse = false;
        }
        saveAndRefresh(true);
        Toast.makeText(this, "Пресет применён", Toast.LENGTH_SHORT).show();
    }

    private void buildShapesChips() {
        chipsShapes.removeAllViews();
        for (int i = 0; i < shapeNames.length; i++) {
            final int bit = 1 << i;
            Chip chip = new Chip(this);
            chip.setText(shapeNames[i]);
            chip.setCheckable(true);
            chip.setChecked((config.shapeMask & bit) != 0);
            chip.setOnCheckedChangeListener((button, checked) -> {
                haptic(chip);
                if (!checked && config.shapeMask == bit) {
                    chip.setChecked(true);
                    Toast.makeText(this, "Выберите хотя бы одну фигуру", Toast.LENGTH_SHORT).show();
                    return;
                }
                config.shapeMask = checked ? (config.shapeMask | bit) : (config.shapeMask & ~bit);
                saveAndRefresh(false);
            });
            chipsShapes.addView(chip);
        }
    }

    private void setupColorsTab() {
        switchDynamic.setOnCheckedChangeListener((btn, checked) -> {
            haptic(switchDynamic);
            config.useDynamicColors = checked;
            cardManualColor.setVisibility(checked ? View.GONE : View.VISIBLE);
            saveAndRefresh(false);
        });

        findViewById(R.id.btnCustomBaseColor).setOnClickListener(v -> {
            haptic(v);
            openColorPickerDialog(0);
        });

        switchSecondary.setOnCheckedChangeListener((btn, checked) -> {
            haptic(switchSecondary);
            config.useSecondary = checked;
            layoutSecondaryControls.setVisibility(checked ? View.VISIBLE : View.GONE);
            saveAndRefresh(false);
        });

        sliderExtraCount.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.extraCount = Math.round(value);
                saveAndRefresh(false);
            }
        });

        btnColorSec1.setOnClickListener(v -> { haptic(v); openColorPickerDialog(1); });
        btnColorSec2.setOnClickListener(v -> { haptic(v); openColorPickerDialog(2); });
        btnColorSec3.setOnClickListener(v -> { haptic(v); openColorPickerDialog(3); });

        switchCustomBg.setOnCheckedChangeListener((btn, checked) -> {
            haptic(switchCustomBg);
            config.customBackground = checked;
            saveAndRefresh(false);
        });

        btnBgColor.setOnClickListener(v -> { haptic(v); openColorPickerDialog(4); });

        btnPickPhoto.setOnClickListener(v -> {
            haptic(v);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            photoPickerLauncher.launch(intent);
        });

        btnClearPhoto.setOnClickListener(v -> {
            haptic(v);
            config.photo = "";
            saveAndRefresh(true);
            Toast.makeText(this, "Фото удалено", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupMotionTab() {
        sliderSpeed.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.rotationSpeed = value;
                saveAndRefresh(false);
            }
        });

        switchPulse.setOnCheckedChangeListener((btn, checked) -> {
            haptic(switchPulse);
            config.pulse = checked;
            saveAndRefresh(false);
        });

        sliderOpacity.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.opacity = value;
                saveAndRefresh(false);
            }
        });

        sliderBlur.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                config.blur = value;
                saveAndRefresh(false);
            }
        });
    }

    private void setupShareTab() {
        findViewById(R.id.btnShowQr).setOnClickListener(v -> {
            haptic(v);
            showQrDialog();
        });

        findViewById(R.id.btnCopyCode).setOnClickListener(v -> {
            haptic(v);
            String code = SettingsCode.encode(config);
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("mUI Live", code));
                Toast.makeText(this, "Код скопирован в буфер", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnScanQr).setOnClickListener(v -> {
            haptic(v);
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Наведите камеру на QR-код настроек mUI Live");
            options.setBeepEnabled(false);
            options.setOrientationLocked(false);
            qrScanner.launch(options);
        });

        findViewById(R.id.btnImportCode).setOnClickListener(v -> {
            haptic(v);
            showImportCodeDialog();
        });

        findViewById(R.id.btnAuthorGithub).setOnClickListener(v -> {
            haptic(v);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rixa8"));
                startActivity(intent);
            } catch (Exception ignored) {
                Toast.makeText(this, "https://github.com/rixa8", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnResetDefaults).setOnClickListener(v -> {
            haptic(v);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Сбросить все настройки?")
                    .setMessage("Все параметры будут возвращены к исходным значениям.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Сбросить", (d, w) -> {
                        config.reset();
                        saveAndRefresh(true);
                        Toast.makeText(this, "Настройки сброшены", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void setupHeaderAndBottomActions() {
        // Quick Randomize (Dice) in Header
        findViewById(R.id.btnHeaderRandom).setOnClickListener(v -> {
            haptic(v);
            randomizeSeed();
        });

        // Quick Fullscreen in Header
        findViewById(R.id.btnHeaderFullscreen).setOnClickListener(v -> {
            haptic(v);
            openFullscreenDialog();
        });

        // Apply Wallpaper Button
        findViewById(R.id.btnApply).setOnClickListener(v -> {
            haptic(v);
            openWallpaperPicker();
        });
    }

    private void randomizeSeed() {
        config.seed = new Random().nextInt(1_000_000);
        saveAndRefresh(false);
        Toast.makeText(this, "Новая композиция (Seed: " + config.seed + ")", Toast.LENGTH_SHORT).show();
    }

    private void saveAndRefresh(boolean rebuildChipsAndColors) {
        config.save(this);
        updateUiFromConfig();
        if (rebuildChipsAndColors) {
            buildShapesChips();
        }
    }

    private void updateUiFromConfig() {
        // Text labels
        valueSize.setText(String.format(Locale.getDefault(), "%d%%", Math.round(config.sizeMultiplier * 100)));
        valuePetals.setText(String.format(Locale.getDefault(), "%d%%", Math.round(config.petalDepth * 100)));
        valueSpacing.setText(String.format(Locale.getDefault(), "%d%%", Math.round(config.spacing * 100)));
        valueSpeed.setText(String.format(Locale.getDefault(), "%.1f×", config.rotationSpeed));
        valueOpacity.setText(String.format(Locale.getDefault(), "%d%%", Math.round(config.opacity * 100)));
        valueBlur.setText(String.format(Locale.getDefault(), "%d%%", Math.round(config.blur * 100)));
        textExtraCount.setText(String.format(Locale.getDefault(), "Количество акцентов: %d", config.extraCount));
        previewSeedText.setText(String.format(Locale.getDefault(), "Seed: %d", config.seed));

        // Sliders & Switches
        sliderSize.setValue(clamp(config.sizeMultiplier, 0.15f, 0.40f));
        sliderPetals.setValue(clamp(config.petalDepth, 0.0f, 0.50f));
        sliderSpacing.setValue(clamp(config.spacing, 0.35f, 1.50f));
        sliderSpeed.setValue(clamp(config.rotationSpeed, 0.2f, 10.0f));
        sliderOpacity.setValue(clamp(config.opacity, 0.10f, 1.0f));
        sliderBlur.setValue(clamp(config.blur, 0.0f, 1.0f));
        sliderExtraCount.setValue(clamp(config.extraCount, 1, 3));

        switchDynamic.setChecked(config.useDynamicColors);
        cardManualColor.setVisibility(config.useDynamicColors ? View.GONE : View.VISIBLE);

        switchSecondary.setChecked(config.useSecondary);
        layoutSecondaryControls.setVisibility(config.useSecondary ? View.VISIBLE : View.GONE);

        switchPulse.setChecked(config.pulse);
        switchCustomBg.setChecked(config.customBackground);

        // Photo status
        boolean hasPhoto = config.photo != null && !config.photo.isEmpty();
        textPhotoStatus.setText(hasPhoto ? "✓ Установлено пользовательское фото" : "Фоновое изображение не выбрано");
        btnClearPhoto.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);

        // Color circles & swatches
        buildBaseColorSwatches();
        updateSecondaryButtons();

        // Feed to preview
        preview.setConfig(config);
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    private void updateSecondaryButtons() {
        btnColorSec1.setTextColor(config.secondaryColor);
        btnColorSec1.setStrokeColor(ColorStateList.valueOf(config.secondaryColor));
        btnColorSec2.setTextColor(config.extra2);
        btnColorSec2.setStrokeColor(ColorStateList.valueOf(config.extra2));
        btnColorSec3.setTextColor(config.extra3);
        btnColorSec3.setStrokeColor(ColorStateList.valueOf(config.extra3));
        btnBgColor.setTextColor(config.backgroundColor);
        btnBgColor.setStrokeColor(ColorStateList.valueOf(config.backgroundColor));
    }

    private void buildBaseColorSwatches() {
        layoutManualColors.removeAllViews();
        for (int color : availableColors) {
            boolean selected = (color == config.baseColor);
            TextView swatch = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(44));
            params.setMarginEnd(dp(10));
            swatch.setLayoutParams(params);
            swatch.setGravity(Gravity.CENTER);
            swatch.setText(selected ? "✓" : "");
            swatch.setTextColor(Color.WHITE);
            swatch.setTextSize(18);

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(color);
            if (selected) {
                circle.setStroke(dp(3), Color.WHITE);
            }
            swatch.setBackground(circle);

            swatch.setOnClickListener(v -> {
                haptic(v);
                config.baseColor = color;
                config.useDynamicColors = false;
                saveAndRefresh(false);
            });
            layoutManualColors.addView(swatch);
        }
    }

    private void openColorPickerDialog(int target) {
        int initialColor;
        if (target == 0) initialColor = config.baseColor;
        else if (target == 1) initialColor = config.secondaryColor;
        else if (target == 2) initialColor = config.extra2;
        else if (target == 3) initialColor = config.extra3;
        else initialColor = config.backgroundColor;

        float[] hsv = new float[3];
        Color.colorToHSV(initialColor, hsv);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null);
        FrameLayout previewBanner = dialogView.findViewById(R.id.pickerPreviewBanner);
        TextView hexText = dialogView.findViewById(R.id.pickerHexText);
        TextView hueText = dialogView.findViewById(R.id.pickerHueValue);
        TextView satText = dialogView.findViewById(R.id.pickerSatValue);
        TextView valText = dialogView.findViewById(R.id.pickerValValue);
        Slider sliderHue = dialogView.findViewById(R.id.sliderHue);
        Slider sliderSat = dialogView.findViewById(R.id.sliderSat);
        Slider sliderVal = dialogView.findViewById(R.id.sliderVal);
        LinearLayout swatchesRow = dialogView.findViewById(R.id.pickerSwatchesRow);

        sliderHue.setValue(hsv[0]);
        sliderSat.setValue(hsv[1] * 100f);
        sliderVal.setValue(hsv[2] * 100f);

        Runnable updateDialog = () -> {
            int currentColor = Color.HSVToColor(hsv);
            previewBanner.setBackgroundColor(currentColor);
            String hex = String.format("#%06X", (0xFFFFFF & currentColor));
            hexText.setText(hex);
            hexText.setTextColor(hsv[2] > 0.6f && hsv[1] < 0.5f ? 0xFF111111 : 0xFFFFFFFF);
            hueText.setText(String.format(Locale.getDefault(), "%d°", Math.round(hsv[0])));
            satText.setText(String.format(Locale.getDefault(), "%d%%", Math.round(hsv[1] * 100f)));
            valText.setText(String.format(Locale.getDefault(), "%d%%", Math.round(hsv[2] * 100f)));
        };

        updateDialog.run();

        // Populate swatches in picker
        for (int c : availableColors) {
            View swatch = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(36), dp(36));
            lp.setMarginEnd(dp(8));
            swatch.setLayoutParams(lp);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(c);
            swatch.setBackground(circle);
            swatch.setOnClickListener(v -> {
                haptic(v);
                Color.colorToHSV(c, hsv);
                sliderHue.setValue(hsv[0]);
                sliderSat.setValue(hsv[1] * 100f);
                sliderVal.setValue(hsv[2] * 100f);
                updateDialog.run();
            });
            swatchesRow.addView(swatch);
        }

        sliderHue.addOnChangeListener((s, v, u) -> {
            if (u) { hsv[0] = v; updateDialog.run(); }
        });
        sliderSat.addOnChangeListener((s, v, u) -> {
            if (u) { hsv[1] = v / 100f; updateDialog.run(); }
        });
        sliderVal.addOnChangeListener((s, v, u) -> {
            if (u) { hsv[2] = v / 100f; updateDialog.run(); }
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Выбор цвета")
                .setView(dialogView)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Применить", (d, w) -> {
                    int chosen = Color.HSVToColor(hsv);
                    if (target == 0) {
                        config.baseColor = chosen;
                        config.useDynamicColors = false;
                    } else if (target == 1) {
                        config.secondaryColor = chosen;
                        config.useSecondary = true;
                    } else if (target == 2) {
                        config.extra2 = chosen;
                        config.useSecondary = true;
                        config.extraCount = Math.max(2, config.extraCount);
                    } else if (target == 3) {
                        config.extra3 = chosen;
                        config.useSecondary = true;
                        config.extraCount = 3;
                    } else {
                        config.backgroundColor = chosen;
                        config.customBackground = true;
                    }
                    saveAndRefresh(false);
                })
                .show();
    }

    private void openFullscreenDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_fullscreen_preview);

        WallpaperPreviewView fsView = dialog.findViewById(R.id.fullscreenPreviewView);
        fsView.setConfig(config);
        fsView.setShowScrim(false);

        TextView title = dialog.findViewById(R.id.fullscreenTitle);
        title.setText(String.format(Locale.getDefault(), "Seed: %d · Свайпайте влево/вправо для параллакса", config.seed));

        dialog.findViewById(R.id.btnFullscreenClose).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnFullscreenDice).setOnClickListener(v -> {
            haptic(v);
            config.seed = new Random().nextInt(1_000_000);
            config.save(this);
            fsView.setConfig(config);
            preview.setConfig(config);
            title.setText(String.format(Locale.getDefault(), "Seed: %d · Свайпайте влево/вправо для параллакса", config.seed));
        });

        dialog.findViewById(R.id.btnFullscreenApply).setOnClickListener(v -> {
            haptic(v);
            openWallpaperPicker();
        });

        dialog.show();
    }

    private void showQrDialog() {
        try {
            String code = SettingsCode.encode(config);
            Bitmap bitmap = new BarcodeEncoder().encodeBitmap(code, com.google.zxing.BarcodeFormat.QR_CODE, 720, 720);

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(Gravity.CENTER);
            container.setPadding(dp(20), dp(10), dp(20), dp(10));

            ImageView iv = new ImageView(this);
            iv.setImageBitmap(bitmap);
            iv.setAdjustViewBounds(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(260), dp(260));
            iv.setLayoutParams(lp);
            container.addView(iv);

            TextView hint = new TextView(this);
            hint.setText("Отсканируйте код на другом устройстве с mUI Live для мгновенного переноса стиля.");
            hint.setTextColor(0xFFB5B3C5);
            hint.setTextSize(13);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0, dp(12), 0, 0);
            container.addView(hint);

            new MaterialAlertDialogBuilder(this)
                    .setTitle("QR-код текущего стиля")
                    .setView(container)
                    .setNeutralButton("Скопировать код", (d, w) -> {
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (cm != null) {
                            cm.setPrimaryClip(ClipData.newPlainText("mUI Live", code));
                            Toast.makeText(this, "Код скопирован", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("Готово", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось сгенерировать QR-код", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportCodeDialog() {
        com.google.android.material.textfield.TextInputLayout layout = new com.google.android.material.textfield.TextInputLayout(this);
        layout.setPadding(dp(24), dp(12), dp(24), 0);
        layout.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setHint("Код настроек mUI Live");

        com.google.android.material.textfield.TextInputEditText input = new com.google.android.material.textfield.TextInputEditText(this);
        input.setSingleLine(true);
        layout.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Импорт настроек")
                .setMessage("Вставьте скопированный текстовый код конфигурации:")
                .setView(layout)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Применить", (d, w) -> {
                    String text = input.getText() != null ? input.getText().toString().trim() : "";
                    try {
                        ShapeConfig decoded = SettingsCode.decode(text, config);
                        config = decoded;
                        saveAndRefresh(true);
                        Toast.makeText(this, "Стиль успешно импортирован", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, "Некорректный код конфигурации", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void openWallpaperPicker() {
        try {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, MyWallpaperService.class));
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            } catch (Exception ignored) {
                Toast.makeText(this, "Не удалось открыть выбор обоев", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void haptic(View view) {
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
