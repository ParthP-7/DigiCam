//
// Created by CainHuang on 2019/3/19.
//

#include <cstring>
#include "FilterManager.h"
#include "Filter.h"

FilterManager *FilterManager::instance = 0;
std::mutex FilterManager::mutex;

FilterManager::FilterManager() {

}

FilterManager::~FilterManager() {

}

FilterManager *FilterManager::getInstance() {
    if (!instance) {
        std::unique_lock<std::mutex> lock(mutex);
        if (!instance) {
            instance = new (std::nothrow) FilterManager();
        }
    }
    return instance;
}

void FilterManager::destroy() {
    if (instance) {
        std::unique_lock<std::mutex> lock(mutex);
        if (instance) {
            delete instance;
            instance = nullptr;
        }
    }
}

GLFilter *FilterManager::getFilter(FilterInfo *filterInfo) {
    if (filterInfo->id != -1) {
        return getFilter(filterInfo->id);
    } else if (filterInfo->name != nullptr) {
        return getFilter(filterInfo->name);
    }
    return nullptr;
}

GLFilter *FilterManager::getFilter(const char *name) {

    // 滤镜特效
    if (!strcmp("Soul", name)) {
        return new GLEffectSoulStuffFilter();
    }
    if (!strcmp("Jitter", name)) {
        return new GLEffectShakeFilter();
    }
    if (!strcmp("Illusion", name)) {
        return new GLEffectIllusionFilter();
    }
    if (!strcmp("Zoom", name)) {
        return new GLEffectScaleFilter();
    }
    if (!strcmp("Flash", name)) {
        return new GLEffectGlitterWhiteFilter();
    }

    // 分屏特效
    if (!strcmp("Fuzzy", name)) {
        return new GLFrameBlurFilter();
    }
    if (!strcmp("B/W Split", name)) {
        return new GLFrameBlackWhiteThreeFilter();
    }
    if (!strcmp("2 screen", name)) {
        return new GLFrameTwoFilter();
    }
    if (!strcmp("3 screen", name)) {
        return new GLFrameThreeFilter();
    }
    if (!strcmp("4 screen", name)) {
        return new GLFrameFourFilter();
    }
    if (!strcmp("6 screen", name)) {
        return new GLFrameSixFilter();
    }
    if (!strcmp("9 screen", name)){
        return new GLFrameNineFilter();
    }
    return nullptr;
}

GLFilter *FilterManager::getFilter(const int id) {
    switch (id) {
        // 滤镜特效
        case 0x000: { // 灵魂出窍
            return new GLEffectSoulStuffFilter();
        }
        case 0x001: { // 抖动
            return new GLEffectShakeFilter();
        }
        case 0x002: { // 幻觉
            return new GLEffectIllusionFilter();
        }
        case 0x003: { // 缩放
            return new GLEffectScaleFilter();
        }
        case 0x004: { // 闪白
            return new GLEffectGlitterWhiteFilter();
        }

        // 分屏特效
        case 0x200: { // 模糊分屏特效
            return new GLFrameBlurFilter();
        }
        case 0x201:{ // 黑白三屏特效
            return new GLFrameBlackWhiteThreeFilter();
        }
        case 0x202: { // 两屏特效
            return new GLFrameTwoFilter();
        }
        case 0x203: { // 三屏特效
            return new GLFrameThreeFilter();
        }
        case 0x204: { // 四屏特效
            return new GLFrameFourFilter();
        }
        case 0x205: { // 六屏特效
            return new GLFrameSixFilter();
        }
        case 0x206: { // 九屏特效
            return new GLFrameNineFilter();
        }
    }
    return nullptr;
}
