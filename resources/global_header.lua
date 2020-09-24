local _vrt_array_init
local _vrt_push_stack_trace
local _vrt_pop_stack_trace
local _vrt_print_stack_trace
local _vrt_call
local _vrt_map_init

local panic
local assert

do
    _vrt_array_init = function(size, typeid, default_value)
        local array = {
            _typeid = typeid,
            data = {},
            length = size
        }

        for i = 1, size do
            array.data[i] = default_value
        end

        local function bounds_check(index)
            if index < 1 or index > array.length then
                panic("array index out of bounds: " .. (index - 1) .. " (length is " .. array.length .. ")")
            end
        end

        setmetatable(array.data, {
            __index = function(t, k)
                bounds_check(k)
                return rawget(t, k)
            end,
            __newindex = function(t, k, v)
                bounds_check(k)
                return rawset(t, k, v)
            end
        })

        setmetatable(array, {
            __tostring = function()
                local result = "[ "
                for i = 1, array.length do
                    result = result .. tostring(array.data[i])

                    if i < array.length then
                        result = result .. ", "
                    end
                end
                return result .. " ]"
            end
        })

        return array
    end

    local stack = {}
    local stack_index = 0

    _vrt_push_stack_trace = function(frame)
        stack_index = stack_index + 1
        stack[stack_index] = frame
    end

    _vrt_pop_stack_trace = function()
        local frame = stack[stack_index]
        stack[stack_index] = nil
        stack_index = stack_index - 1
        return frame
    end

    _vrt_print_stack_trace = function()
        while stack_index > 0 do
            local frame = _vrt_pop_stack_trace()
            print("    at " .. frame.name .. "(" .. frame.file .. ":" .. frame.line .. ")")
        end
    end

    _vrt_call = function(func, stack_trace_frame, ...)
        _vrt_push_stack_trace(stack_trace_frame)
        local result = { func(...) }
        _vrt_pop_stack_trace()
        return unpack(result)
    end

    _vrt_map_init = function()
        local map = {}

        setmetatable(map, {
            __tostring = function()
                local r = "{ "

                local count = 0
                for _, _ in pairs(map) do
                    count = count + 1
                end

                local i = 0
                for k, v in pairs(map) do
                    r = r .. tostring(k) .. "=" .. tostring(v)

                    i = i + 1
                    if i < count then
                        r = r .. ", "
                    end
                end

                return r .. " }"
            end
        })

        return map
    end

    panic = function(message)
        if message then
            print(message)
        else
            print("runtime panic")
        end

        _vrt_print_stack_trace()
        error()
    end

    assert = function(condition, message)
        if not condition then
            if message then
                panic("assertion failed: " .. message)
            else
                panic("assertion failed")
            end
        end
    end
end